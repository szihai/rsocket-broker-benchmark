package com.netifi.acmeair;

import com.google.protobuf.Empty;
import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.spi.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PostgresCustomerService implements CustomerService {
  static final Logger logger = LogManager.getLogger(PostgresCustomerService.class);

  private static final ValidateCustomerResponse VALID =
      ValidateCustomerResponse.newBuilder().setValid(true).build();

  private static final ValidateCustomerResponse INVALID =
      ValidateCustomerResponse.newBuilder().setValid(false).build();

  private final Pool<PostgresqlConnection> connectionPool;

  @Autowired
  public PostgresCustomerService(Pool<PostgresqlConnection> connectionPool) {
    this.connectionPool = connectionPool;
  }

  @Override
  public Mono<Empty> createCustomer(CreateCustomerRequest message, ByteBuf metadata) {

    Customer customer = message.getCustomer();

    Address address = customer.getAddress();

    return connectionPool
        .member()
        .flatMap(
            member -> {
              Publisher<? extends Result> insertCustomer =
                  member
                      .value()
                      .createStatement("insert into customer values ($1, $2, $3, $4, $5, $6, $7)")
                      .bind(0, message.getUsername())
                      .bind(1, message.getPassword())
                      .bind(2, customer.getStatus())
                      .bind(3, customer.getTotalMiles())
                      .bind(4, customer.getMilesYtd())
                      .bind(5, customer.getPhoneNumber())
                      .bind(6, customer.getPhoneNumberType())
                      .execute();

              Publisher<? extends Result> insertAddress =
                  member
                      .value()
                      .createStatement(
                          "insert into address values ($1, $2, $3, $4, $5, $6, $7, $8)")
                      .bind(0, KeyGenerator.generateKey())
                      .bind(1, message.getUsername())
                      .bind(2, address.getStreetAddress1())
                      .bind(3, address.getStreetAddress2())
                      .bind(4, address.getCity())
                      .bind(5, address.getStateProvince())
                      .bind(6, address.getCountry())
                      .bind(7, address.getPostalCode())
                      .execute();

              return Flux.merge(insertCustomer, insertAddress)
                  .then()
                  .thenReturn(Empty.getDefaultInstance())
                  .doFinally(s -> member.checkin());
            })
        .doOnError(throwable -> logger.error("error creating customer"));
  }

  @Override
  public Mono<Empty> updateCustomer(UpdateCustomerRequest message, ByteBuf metadata) {
    Customer customer = message.getCustomer();

    Address address = customer.getAddress();

    return connectionPool
        .member()
        .flatMap(
            member -> {
              Publisher<? extends Result> insertCustomer =
                  member
                      .value()
                      .createStatement(
                          "update customer "
                              + "set status = $1, "
                              + "totalmiles = $2, "
                              + "mileytd = $3, "
                              + "phonenumber = $4, "
                              + "phonenumbertype = $5 where username = $6")
                      .bind(0, customer.getStatus())
                      .bind(1, customer.getTotalMiles())
                      .bind(2, customer.getMilesYtd())
                      .bind(3, customer.getPhoneNumber())
                      .bind(4, customer.getPhoneNumberType())
                      .bind(5, message.getUsername())
                      .execute();

              Publisher<? extends Result> insertAddress =
                  member
                      .value()
                      .createStatement(
                          "update address set streetaddress1 = $1 streetaddress2 = $2 city = $3 stateProvince = $4 country = $5 postcode = $6 where username = $7")
                      .bind(0, address.getStreetAddress1())
                      .bind(1, address.getStreetAddress2())
                      .bind(2, address.getCity())
                      .bind(3, address.getStateProvince())
                      .bind(4, address.getCountry())
                      .bind(5, address.getPostalCode())
                      .bind(6, message.getUsername())
                      .execute();

              return Flux.merge(insertCustomer, insertAddress)
                  .then()
                  .thenReturn(Empty.getDefaultInstance())
                  .doFinally(s -> member.checkin());
            })
        .doOnError(throwable -> logger.error("error updating customer", throwable));
  }

  @Override
  public Mono<GetCustomerResponse> getCustomer(GetCustomerRequest message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement(
                            "select c.*, a.* from customer c join address a on c.username = a.username where c.username = $1 limit 1")
                        .bind(0, message.getUsername())
                        .execute())
                    .flatMap(
                        result ->
                            result.map(
                                (row, rowMetadata) -> {
                                  Address.Builder address =
                                      Address.newBuilder()
                                          .setStreetAddress1(
                                              row.get("streetaddress1", String.class))
                                          .setStreetAddress2(
                                              row.get("streetaddress2", String.class))
                                          .setCity(row.get("city", String.class))
                                          .setStateProvince(row.get("stateprovince", String.class))
                                          .setCountry(row.get("country", String.class))
                                          .setPostalCode(row.get("postcode", String.class));

                                  Customer.Builder customer =
                                      Customer.newBuilder()
                                          .setAddress(address)
                                          .setStatus(row.get("status", String.class))
                                          .setTotalMiles(row.get("totalmiles", Integer.class))
                                          .setMilesYtd(row.get("mileytd", Integer.class))
                                          .setPhoneNumber(row.get("phonenumber", String.class))
                                          .setPhoneNumberType(
                                              row.get("phonenumbertype", String.class));

                                  return GetCustomerResponse.newBuilder()
                                      .setCustomer(customer)
                                      .build();
                                }))
                    .switchIfEmpty(Mono.empty())
                    .single()
                    .doFinally(s -> member.checkin()))
        .doOnError(throwable -> logger.error("error getting customer {}", message));
  }

  @Override
  public Mono<ValidateCustomerResponse> validateCustomer(
      ValidateCustomerRequest message, ByteBuf metadata) {
    logger.debug("validating user {}", message.getUsername());
    return connectionPool
        .member()
        .flatMap(
            member ->
                Flux.from(
                    member
                        .value()
                        .createStatement(
                            "select username from CUSTOMER where username = $1 and password = $2 limit 1")
                        .bind(0, message.getUsername())
                        .bind(1, message.getPassword())
                        .execute())
                    .count()
                    .map(
                        count -> {
                          if (count == 1) {
                            logger.debug("user {} is VALID", message.getUsername());
                            return VALID;
                          } else {
                            logger.debug("user {} is INVALID", message.getUsername());
                            return INVALID;
                          }
                        })
                    .doFinally(s -> member.checkin()))
        .doOnError(throwable -> logger.error("error validating customer {}", message));
  }
}
