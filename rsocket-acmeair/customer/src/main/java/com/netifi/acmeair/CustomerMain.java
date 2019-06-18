package com.netifi.acmeair;

import com.netifi.reactor.pool.NonBlockingPoolFactory;
import com.netifi.reactor.pool.PartitionedThreadPool;
import com.netifi.reactor.pool.Pool;
import com.netifi.reactor.pool.r2dbc.R2DbcPoolManager;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class CustomerMain {

  static final Logger logger = LogManager.getLogger(CustomerMain.class);

  @Value("${netifi.acmeair.postgres.host}")
  private String host;

  @Value("${netifi.acmeair.postgres.database}")
  private String database;

  @Value("${netifi.acmeair.postgres.username}")
  private String username;

  @Value("${netifi.acmeair.postgres.password}")
  private String password;

  @Value("${netifi.acmeair.postgres.poolSize}")
  private int poolSize;

  public static void main(String... args) {
    SpringApplication.run(CustomerMain.class, args);
  }

  @Bean
  PostgresqlConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host(host)
            .port(5432)
            .applicationName("customer")
            .database(database)
            .username(username)
            .password(password)
            .build());
  }

  @Bean
  Pool<PostgresqlConnection> connectionPool(PostgresqlConnectionFactory connectionFactory) {
    R2DbcPoolManager poolManager = new R2DbcPoolManager(connectionFactory);
    return new PartitionedThreadPool<>(
        new NonBlockingPoolFactory<PostgresqlConnection>(
            poolSize, 1024, Duration.ofSeconds(60), Duration.ofSeconds(120), poolManager));
  }

  /*@Bean
  ConnectionPool connectionPool(PostgresqlConnectionFactory connectionFactory) {
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .customizer(connectionPoolBuilder ->
            connectionPoolBuilder.threadAffinity(true))
        .maxIdleTime(Duration.ofSeconds(60))
        .maxSize(poolSize)
        .build();

    return new ConnectionPool(configuration);
  }*/
}
