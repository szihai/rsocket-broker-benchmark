package com.netifi.acmeair;

import com.google.protobuf.Empty;
import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.postgresql.PostgresqlResult;
import io.r2dbc.spi.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.netifi.acmeair.loader.FlightsData;
import com.netifi.acmeair.loader.FlightsLoader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.netifi.acmeair.loader.FlightsData.Airport;
import com.netifi.acmeair.loader.FlightsData.Flight;
import com.netifi.acmeair.loader.FlightsData.FlightSegment;

@Component
public class PostgresFlightLoaderService implements FlightLoaderService {
  private final Logger logger = LogManager.getLogger(PostgresFlightLoaderService.class);

  private final Pool<PostgresqlConnection> connectionPool;

  private FlightsLoader flightsLoader;

  @Autowired
  public PostgresFlightLoaderService(
      Pool<PostgresqlConnection> connectionPool, FlightsLoader flightsLoader) {
    this.connectionPool = connectionPool;
    this.flightsLoader = flightsLoader;
  }

  private static DropFlightsResponse newResponse(long duration) {
    return DropFlightsResponse.newBuilder().setDurationMillis(duration).build();
  }

  @Override
  public Mono<LoadFlightsResponse> loadFlights(LoadFlightsRequest message, ByteBuf metadata) {
    return loadFlights(message.getResource());
  }

  @Override
  public Mono<LoadFlightsResponse> loadDefaultFlights(Empty message, ByteBuf metadata) {
    return loadFlights("mileage.csv");
  }

  private Mono<LoadFlightsResponse> loadFlights(String name) {
    return dropFlights()
        .then(
            Mono.defer(
                () -> {
                  long start = now();

                  FlightsData flightsData = flightsLoader.load(name);

                  Flux<Void> insertFlights =
                      flightsData.flights().flatMap(this::insertNewFlightDocument);

                  Flux<Void> insertAirports =
                      flightsData
                          .airports()
                          .distinct(Airport::getCode)
                          .flatMap(this::insertAirportDocument);

                  Flux<Void> insertFlightSegments =
                      flightsData.flightSegments().flatMap(this::insertNewFlightSegmentDocument);

                  return Flux.merge(insertFlights, insertAirports, insertFlightSegments)
                      .then(Mono.just(newFlightsResponse(now() - start)));
                }))
        .log();
  }

  private LoadFlightsResponse newFlightsResponse(long duration) {
    return LoadFlightsResponse.newBuilder().setDurationMillis(duration).build();
  }

  private Mono<Void> insertNewFlightDocument(Flight flight) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement(
                            "insert into flight values ($1, $2, $3, $4, $5, $6, $7, $8)")
                        .bind(0, flight.getFlightId())
                        .bind(1, flight.getFirstClassBaseCost())
                        .bind(2, flight.getFirstClassSeatsNum())
                        .bind(3, flight.getEconomyClassBaseCost())
                        .bind(4, flight.getEconomyClassSeatsNum())
                        .bind(5, flight.getAirplaneTypeId())
                        .bind(6, flight.getDepartureTime())
                        .bind(7, flight.getArrivalTime())
                        .execute())
                    .then()
                    .doFinally(s -> member.checkin()))
        .doOnError(throwable -> logger.error("error inserting flight", throwable));
  }

  private Mono<Void> insertAirportDocument(Airport airport) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement("insert into airport values ($1, $2)")
                        .bind(0, airport.getCode())
                        .bind(1, airport.getName())
                        .execute())
                    .then()
                    .doFinally(s -> member.checkin()))
        .doOnError(throwable -> logger.error("error inserting airport", throwable));
  }

  private Mono<Void> insertNewFlightSegmentDocument(FlightSegment flightSegment) {
    return connectionPool
        .member()
        .flatMap(
            member -> {
              String flightId = flightSegment.getFlightId();
              String flightSegmentId = KeyGenerator.generateKey();

              Publisher<? extends Result> insertFlightSegment =
                  member
                      .value()
                      .createStatement("insert into FLIGHT_SEGMENT values ($1, $2, $3, $4)")
                      .bind(0, flightSegmentId)
                      .bind(1, flightSegment.getSrcAirportCode())
                      .bind(2, flightSegment.getDestAirportCode())
                      .bind(3, flightSegment.getMiles())
                      .execute();

              Publisher<? extends Result> insertFlightToFlightSegment =
                  member
                      .value()
                      .createStatement("insert into FLIGHT_TO_FLIGHT_SEGMENT values ($1, $2)")
                      .bind(0, flightId)
                      .bind(1, flightSegmentId)
                      .execute();

              return Flux.merge(insertFlightSegment, insertFlightToFlightSegment)
                  .doFinally(s -> member.checkin())
                  .then();
            })
        .doOnError(throwable -> logger.error("error inserting flight segment", throwable));
  }

  @Override
  public Mono<DropFlightsResponse> dropFlights(Empty message, ByteBuf metadata) {
    return dropFlights();
  }

  private Mono<DropFlightsResponse> dropFlights() {
    long start = now();
    return connectionPool
        .member()
        .flatMapMany(
            member -> {
              Flux<PostgresqlResult> deleteFlights =
                  member.value().createStatement("delete from FLIGHT").execute();

              Flux<PostgresqlResult> deleteFromFlightToFlightSegment =
                  member.value().createStatement("delete from FLIGHT_TO_FLIGHT_SEGMENT").execute();

              Flux<PostgresqlResult> deleteFromFlightSegment =
                  member.value().createStatement("delete from FLIGHT_SEGMENT").execute();
              return Flux.merge(
                  deleteFlights, deleteFromFlightToFlightSegment, deleteFromFlightSegment)
                  .doFinally(s -> member.checkin())
                  .then()
                  .thenReturn(newResponse(now() - start));
            })
        .single()
        .doOnError(throwable -> logger.error("error dropping flights", throwable));
  }

  private long now() {
    return System.currentTimeMillis();
  }
}
