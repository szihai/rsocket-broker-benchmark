package com.netifi.acmeair;

import com.google.protobuf.Empty;
import com.netifi.reactor.pool.Pool;
import com.netifi.spring.core.annotation.Group;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.rsocket.exceptions.ApplicationErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Component
public class PostgresLoginService implements LoginService {

  static final Logger logger = LogManager.getLogger(PostgresLoginService.class);
  static final int DAYS_TO_ALLOW_SESSION = 1;

  private final Pool<PostgresqlConnection> connectionPool;

  @Group("acmeair.proteus.customer")
  CustomerServiceClient customerServiceClient;

  @Autowired
  public PostgresLoginService(Pool<PostgresqlConnection> connectionPool) {
    this.connectionPool = connectionPool;
  }

  @Override
  public Mono<LoginResponse> login(LoginRequest message, ByteBuf metadata) {
    if (logger.isDebugEnabled()) {
      logger.debug("user {} is attempting to login", message.getUsername());
    }
    return customerServiceClient
        .validateCustomer(
            ValidateCustomerRequest.newBuilder()
                .setUsername(message.getUsername())
                .setPassword(message.getPassword())
                .build())
        .switchIfEmpty(
            Mono.error(new IllegalAccessError("login failed for user " + message.getUsername())))
        .flatMap(
            response -> {
              if (response.getValid()) {
                String sessionId = UUID.randomUUID().toString();
                Date creation = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(creation);
                c.add(Calendar.DAY_OF_YEAR, DAYS_TO_ALLOW_SESSION);
                Date expiration = c.getTime();
                return connectionPool
                    .member()
                    .flatMap(
                        member ->
                            Flux.from(
                                member
                                    .value()
                                    .createStatement(
                                        "insert into login values ($1, $2, $3, $4)")
                                    .bind(0, sessionId)
                                    .bind(1, message.getUsername())
                                    .bind(2, creation)
                                    .bind(3, expiration)
                                    .execute())
                                .doFinally(s -> member.checkin())
                                .then())
                    .thenReturn(LoginResponse.newBuilder().setSessionId(sessionId).build());
              } else {
                throw new ApplicationErrorException(
                    "login failed for user " + message.getUsername());
              }
            })
        .doOnError(throwable -> logger.error("error logging in", throwable));
  }

  @Override
  public Mono<Empty> logout(LogoutRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement(
                            "update LOGIN set logouttime = $1 where sessionid = $2")
                        .bind(0, new Date())
                        .bind(1, message.getSessionId())
                        .execute())
                    .doFinally(s -> member.checkin())
                    .then())
        .onErrorResume(
            throwable -> {
              logger.error("error logging out", throwable);
              return Mono.empty();
            })
        .thenReturn(Empty.getDefaultInstance());
  }
}
