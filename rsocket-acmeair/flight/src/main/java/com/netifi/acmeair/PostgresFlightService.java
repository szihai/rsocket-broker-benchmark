package com.netifi.acmeair;

import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

class PostgresFlightService implements FlightService {
  static final Logger logger = LogManager.getLogger(PostgresFlightService.class);
  private final Pool<PostgresqlConnection> connectionPool;

  PostgresFlightService(Pool<PostgresqlConnection> connectionPool) {
    this.connectionPool = connectionPool;
  }

  private static Flight toFlight(Row row, RowMetadata rowMetadata) {
    return Flight.newBuilder()
        .setFirstClassBaseCost(String.valueOf(row.get("firstclassbasecost", Integer.class)))
        .setNumFirstClassSeats(String.valueOf(row.get("numfirstclassseats", Integer.class)))
        .setEconomyClassBaseCost(String.valueOf(row.get("economyclassbasecost", Integer.class)))
        .setNumEconomyClassSeats(String.valueOf(row.get("numeconomyclassseats", Integer.class)))
        .setAirplaneTypeId(row.get("airplanetypeid", String.class))
        .setScheduledDepartureTime(row.get("scheduleddeparturetime", Date.class).toString())
        .setScheduledArrivalTime(row.get("scheduledarrivaltime", Date.class).toString())
        .setSegment(
            Segment.newBuilder()
                .setDestinationPort(row.get("destport", String.class))
                .setOriginPort(row.get("originport", String.class))
                .setFlightSegmentId(row.get("flightsegmentid", String.class)))
        .build();
  }

  @Override
  public final Mono<GetTripFlightsResponse> getTripsFlight(
      GetTripFlightsRequest message, ByteBuf metadata) {
    Date fromDate = new Date(message.getFromDate());

    Mono<GetTripFlightsResponse.Builder> map =
        getFlight(message.getFromAirport(), message.getToAirport(), fromDate)
            .map(flight -> GetTripFlightsResponse.newBuilder().addToFlight(flight));

    if (!message.getOneWay()) {
      Date returnDate = new Date(message.getReturnDate());
      map.flatMap(
          builder ->
              getFlight(message.getToAirport(), message.getFromAirport(), returnDate)
                  .map(builder::addReturnFlight));
    }

    return map.map(GetTripFlightsResponse.Builder::build)
        .doOnError(
            throwable ->
                logger.error(
                    "received error - " + message.toString() + " -> date -> " + fromDate.toString(),
                    throwable));
  }

  Mono<Flight> getFlight(String from, String to, Date fromDate) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement(
                            " SELECT fs.*, f.* FROM flight_to_flight_segment ffs "
                                + "JOIN flight_segment fs ON  ffs.flightsegmentid = fs.flightsegmentid "
                                + "JOIN flight f ON ffs.flightid = f.flightid "
                                + "WHERE fs.originport = $1 AND fs.destport = $2 AND f.scheduleddeparturetime = $3")
                        .bind(0, from)
                        .bind(1, to)
                        .bind(2, fromDate)
                        .execute())
                    .doFinally(s -> member.checkin())
                    .switchIfEmpty(Flux.empty())
                    .flatMap(result -> result.map(PostgresFlightService::toFlight))
                    .single());
  }
}
