package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SeedLeaseTest {
    @Test
    void leaseIsActiveUntilItsContributionTimeExpires() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        SeedLease lease = SeedLease.start(start, Duration.ofHours(1));

        assertTrue(lease.isActive(start.plusSeconds(3599)));
        assertFalse(lease.isActive(start.plusSeconds(3600)));
        assertTrue(lease.mayDeleteStoredData(start.plusSeconds(3600)));
    }
}
