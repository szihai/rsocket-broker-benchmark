package com.netifi.acmeair;

import com.google.protobuf.Empty;
import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
public class PostgresBookingService implements BookingService {
  static final Logger logger = LogManager.getLogger(PostgresBookingService.class);

  private final Pool<PostgresqlConnection> connectionPool;

  @Autowired
  public PostgresBookingService(Pool<PostgresqlConnection> connectionPool) {
    this.connectionPool = connectionPool;
  }

  private static Booking toBooking(Row row, RowMetadata rowMetadata) {
    return Booking.newBuilder()
        .setBookingId(row.get("bookingid", String.class))
        .setUsername(row.get("username", String.class))
        .setFlightId(row.get("flightid", String.class))
        .setDateOfBooking(row.get("dateofbooking", Date.class).toString())
        .build();
  }

  @Override
  public Mono<BookFlightResponse> bookFlight(BookFlightRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member -> {
              String toBookingId = KeyGenerator.generateKey();
              String returnBookingId = KeyGenerator.generateKey();
              Publisher<? extends Result> toFlight =
                  member
                      .value()
                      .createStatement("insert into booking values($1, $2, $3, $4)")
                      .bind(0, toBookingId)
                      .bind(1, message.getUsername())
                      .bind(2, message.getReturnFlightId())
                      .bind(3, new Date())
                      .execute();

              Publisher<? extends Result> returnFlight =
                  member
                      .value()
                      .createStatement("insert into booking values($1, $2, $3, $4)")
                      .bind(0, returnBookingId)
                      .bind(1, message.getUsername())
                      .bind(2, message.getReturnFlightId())
                      .bind(3, new Date())
                      .execute();
              return Flux.merge(toFlight, returnFlight)
                  .doFinally(s -> member.checkin())
                  .then()
                  .thenReturn(
                      BookFlightResponse.newBuilder()
                          .setToBookingId(toBookingId)
                          .setReturnBookingId(returnBookingId)
                          .build());
            })
        .doOnError(throwable -> logger.error("error booking flight", throwable));
  }

  @Override
  public Mono<BookOnewayFlightResponse> bookOnewayFlight(
      BookOnewayFlightRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member -> {
              String toBookingId = KeyGenerator.generateKey();
              return Flux.from(
                  member
                      .value()
                      .createStatement("insert into booking values($1, $2, $3, $4)")
                      .bind(0, toBookingId)
                      .bind(1, message.getUsername())
                      .bind(2, message.getToFlightId())
                      .bind(3, new Date())
                      .execute())
                  .doFinally(s -> member.checkin())
                  .then()
                  .thenReturn(
                      BookOnewayFlightResponse.newBuilder().setToBookingId(toBookingId).build());
            })
        .doOnError(throwable -> logger.error("error booking flight", throwable));
  }

  @Override
  public Mono<Booking> getBookingByNumber(GetBookingByNumberRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement("select * from booking where bookingId = $1")
                        .bind(0, message.getBookingId())
                        .execute())
                    .doFinally(s -> member.checkin())
                    .flatMap(result -> result.map(PostgresBookingService::toBooking))
                    .single())
        .doOnError(throwable -> logger.error("error getting by number"));
  }

  @Override
  public Flux<Booking> getBookingsByUser(GetBookingsByUserRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMapMany(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement("select * from booking where username = $1")
                        .bind(0, message.getUsername())
                        .execute())
                    .doFinally(s -> member.checkin())
                    .flatMap(result -> result.map(PostgresBookingService::toBooking)))
        .doOnError(throwable -> logger.error("error getting by number"))
        .onBackpressureBuffer();
  }

  @Override
  public Mono<Empty> cancelBooking(CancelBookingRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement("delete from booking where bookingId = $1")
                        .bind(0, message.getBookingId())
                        .execute())
                    .doFinally(s -> member.checkin())
                    .then())
        .doOnError(throwable -> logger.error("error getting by number"))
        .thenReturn(Empty.getDefaultInstance());
  }
}
