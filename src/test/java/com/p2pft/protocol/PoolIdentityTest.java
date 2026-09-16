package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class PoolIdentityTest {
    @Test
    void savesAndLoadsAKeyPairAndPoolFromYaml() throws Exception {
        var identity = PoolIdentity.generate("research-pool");
        var file = Files.createTempFile("p2pft", ".yaml");

        PoolConfig.save(file, identity);
        PoolIdentity loaded = PoolConfig.load(file).identity();

        assertEquals(identity.peerId(), loaded.peerId());
        assertEquals("research-pool", PoolConfig.load(file).pool());
        assertTrue(loaded.verify("join".getBytes(), loaded.sign("join".getBytes())));
    }
}
