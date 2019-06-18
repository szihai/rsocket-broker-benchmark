package com.netifi.acmeair;

import com.netifi.reactor.pool.Pool;
import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.PostgresqlConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FlightServiceDelegate implements FlightService {
  private static final Logger logger = LogManager.getLogger(FlightServiceDelegate.class);
  private FlightService delegate;

  @Autowired
  public FlightServiceDelegate(
      Pool<PostgresqlConnection> connectionPool,
      @Qualifier("flightCache") boolean enableFlightCache) {
    if (enableFlightCache) {
      logger.info("flight service caching is enabled");
      delegate = new CachedFlightService(connectionPool);
    } else {
      logger.info("flight service caching is not enabled");
      delegate = new PostgresFlightService(connectionPool);
    }
  }

  @Override
  public Mono<GetTripFlightsResponse> getTripsFlight(
      GetTripFlightsRequest message, ByteBuf metadata) {
    return delegate.getTripsFlight(message, metadata);
  }
}
