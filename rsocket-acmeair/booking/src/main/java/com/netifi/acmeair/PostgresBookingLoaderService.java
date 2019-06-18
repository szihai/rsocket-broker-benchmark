package com.netifi.acmeair;

import com.google.protobuf.Empty;
import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PostgresBookingLoaderService implements BookingLoaderService {

  private final Pool<PostgresqlConnection> connectionPool;

  @Autowired
  public PostgresBookingLoaderService(Pool<PostgresqlConnection> connectionPool) {
    this.connectionPool = connectionPool;
  }

  @Override
  public Mono<DroppedBookings> dropBookings(Empty message, ByteBuf metadata) {
    long start = System.currentTimeMillis();
    return connectionPool
        .member()
        .flatMap(
            member ->
                member
                    .value()
                    .createStatement("delete from booking")
                    .execute()
                    .doFinally(s -> member.checkin())
                    .then()
                    .thenReturn(
                        DroppedBookings.newBuilder()
                            .setDurationMillis(System.currentTimeMillis() - start)
                            .build()));
  }
}
