package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PoolProtocolTest {
    @Test
    void peerSignsPoolJoinProofAndAnotherPeerCanVerifyIt() {
        PoolIdentity peer = PoolIdentity.generate("pool");
        byte[] proof = "pool:pool:nonce".getBytes(StandardCharsets.UTF_8);

        byte[] signature = peer.sign(proof);

        assertTrue(peer.verify(proof, signature));
        assertFalse(peer.verify("pool:pool:changed".getBytes(StandardCharsets.UTF_8), signature));
    }
}
