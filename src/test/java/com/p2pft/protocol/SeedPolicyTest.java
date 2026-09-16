package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeedPolicyTest {
    @Test
    void nodeCanRequestOnlyWhatItsSeedContributionCovers() {
        assertTrue(SeedPolicy.canRequest(1_000, 1_000));
        assertFalse(SeedPolicy.canRequest(1_001, 1_000));
    }
}
