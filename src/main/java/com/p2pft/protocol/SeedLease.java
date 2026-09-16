package com.p2pft.protocol;

import java.time.Duration;
import java.time.Instant;

public record SeedLease(Instant startedAt, Instant expiresAt) {
    public static SeedLease start(Instant startedAt, Duration duration) {
        if (startedAt == null || duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("seed lease must have a positive duration");
        }
        return new SeedLease(startedAt, startedAt.plus(duration));
    }

    public boolean isActive(Instant now) { return now.isBefore(expiresAt); }
    public boolean mayDeleteStoredData(Instant now) { return !isActive(now); }
}
