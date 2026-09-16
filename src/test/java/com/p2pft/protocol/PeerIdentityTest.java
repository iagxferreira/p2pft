package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerIdentityTest {
    @Test
    void createsAStableGeneratedHashIdentity() {
        PeerIdentity identity = PeerIdentity.generate();

        assertEquals(64, identity.value().length());
        assertTrue(identity.value().matches("[0-9a-f]{64}"));
        assertEquals(identity.value(), identity.toString());
    }

    @Test
    void generatedPeersDoNotShareAnIdentity() {
        assertNotEquals(PeerIdentity.generate(), PeerIdentity.generate());
    }
}
