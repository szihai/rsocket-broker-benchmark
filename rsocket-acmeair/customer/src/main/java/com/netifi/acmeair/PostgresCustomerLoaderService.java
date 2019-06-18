package com.netifi.acmeair;

import com.google.protobuf.Empty;
import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.PostgresqlConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PostgresCustomerLoaderService implements CustomerLoaderService {
  static final Logger logger = LogManager.getLogger(PostgresCustomerLoaderService.class);
  private static final int CUSTOMER_SERVICE_CONCURRENCY = 32;
  private final Pool<PostgresqlConnection> connectionPool;
  private final CustomerService customerService;
  private final int defaultCustomersCount;

  @Autowired
  public PostgresCustomerLoaderService(
      Pool<PostgresqlConnection> connectionPool,
      CustomerService customerService,
      @Value("${netifi.proteus.defaultCustomersCount}") int defaultCustomersCount) {
    this.connectionPool = connectionPool;
    this.customerService = customerService;
    this.defaultCustomersCount = defaultCustomersCount;
  }

  private static CreateCustomerRequest newCustomer(int id) {

    Address address =
        Address.newBuilder()
            .setStreetAddress1("123 Main St.")
            .setCity("Anytown")
            .setStateProvince("NC")
            .setCountry("USA")
            .setPostalCode("27617")
            .build();

    Customer customer =
        Customer.newBuilder()
            .setStatus("GOLD")
            .setTotalMiles(1_000_000)
            .setMilesYtd(1_000)
            .setPhoneNumber("919-123-4567")
            .setPhoneNumberType("BUSINESS")
            .setAddress(address)
            .build();

    return CreateCustomerRequest.newBuilder()
        .setUsername("uid" + id + "@email.com")
        .setPassword("password")
        .setCustomer(customer)
        .build();
  }

  private static long now() {
    return System.currentTimeMillis();
  }

  @Override
  public Mono<CreateCustomersResponse> createCustomers(
      CreateCustomersRequest message, ByteBuf metadata) {
    return createCustomers(message.getCount());
  }

  @Override
  public Mono<CreateCustomersResponse> createDefaultCustomers(Empty message, ByteBuf metadata) {
    return createCustomers(defaultCustomersCount);
  }

  private Mono<CreateCustomersResponse> createCustomers(int count) {
    return dropCustomers(Empty.getDefaultInstance(), null)
        .then(
            Mono.defer(
                () -> {
                  long start = now();
                  return Flux.range(1, count)
                      .flatMap(
                          v ->
                              customerService.createCustomer(newCustomer(v), Unpooled.EMPTY_BUFFER),
                          CUSTOMER_SERVICE_CONCURRENCY)
                      .then(Mono.just(newCustomerResponse(now() - start)));
                }));
  }

  private CreateCustomersResponse newCustomerResponse(long duration) {
    return CreateCustomersResponse.newBuilder().setDuration(duration).build();
  }

  @Override
  public Mono<DropCustomersResponse> dropCustomers(Empty message, ByteBuf metadata) {
    return connectionPool
        .member()
        .flatMap(
            member ->
                member
                    .value()
                    .createStatement("delete from customer")
                    .execute()
                    .doFinally(s -> member.checkin())
                    .then())
        .thenReturn(DropCustomersResponse.getDefaultInstance());
  }
}
