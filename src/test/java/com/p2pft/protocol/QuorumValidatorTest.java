package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuorumValidatorTest {
    @Test
    void acceptsDigestOnlyWithByzantineMajority() {
        String digest = "abc";

        assertTrue(QuorumValidator.hasQuorum(
                List.of(new PeerReport("a", digest), new PeerReport("b", digest), new PeerReport("c", "bad")), 3));
    }

    @Test
    void rejectsWhenNoDigestReachesRequiredQuorum() {
        assertFalse(QuorumValidator.hasQuorum(
                List.of(new PeerReport("a", "one"), new PeerReport("b", "two"), new PeerReport("c", "three")), 3));
    }

    @Test
    void requiresStrictByzantineMajority() {
        assertFalse(QuorumValidator.hasQuorum(
                List.of(new PeerReport("a", "same"), new PeerReport("b", "same"), new PeerReport("c", "bad"),
                        new PeerReport("d", "bad")), 4));
    }
}
