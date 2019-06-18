package com.netifi.acmeair;

import com.netifi.reactor.pool.Pool;
import io.r2dbc.postgresql.PostgresqlConnection;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class CachedFlightService extends PostgresFlightService {
  private final Map<CacheKey, Mono<Flight>> cachedFlights;

  CachedFlightService(Pool<PostgresqlConnection> connectionPool) {
    super(connectionPool);
    this.cachedFlights = new ConcurrentHashMap<>();
  }

  @Override
  Mono<Flight> getFlight(String from, String to, Date fromDate) {
    return cachedFlights.computeIfAbsent(
        new CacheKey(from, to, fromDate),
        k ->
            super.getFlight(from, to, fromDate)
                .doOnError(throwable -> cachedFlights.remove(k))
                .cache());
  }

  private static final class CacheKey {
    private final String from;
    private final String to;
    private final Date date;

    private CacheKey(String from, String to, Date date) {
      this.from = from;
      this.to = to;
      this.date = date;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CacheKey cacheKey = (CacheKey) o;
      return Objects.equals(from, cacheKey.from)
          && Objects.equals(to, cacheKey.to)
          && Objects.equals(date, cacheKey.date);
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to, date);
    }
  }
}
