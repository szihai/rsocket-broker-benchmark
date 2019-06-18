package com.netifi.acmeair;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class KeyGenerator {
  private static final AtomicLong COUNT = new AtomicLong();
  private static final long HIGH;

  static {
    SecureRandom random = new SecureRandom();
    HIGH = random.nextLong();
  }

  public static String generateKey() {
    return new UUID(HIGH, COUNT.incrementAndGet()).toString();
  }
}
