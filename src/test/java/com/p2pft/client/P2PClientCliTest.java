package com.p2pft.client;

import com.p2pft.protocol.PoolConfig;
import com.p2pft.protocol.PoolIdentity;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class P2PClientCliTest {
    @Test
    void configCarriesUploadEndpointAndClientFileDirectory() throws Exception {
        var directory = Files.createTempDirectory("p2pft-files");
        var configFile = Files.createTempFile("p2pft", ".yaml");
        var identity = PoolIdentity.generate("pool");

        PoolConfig.save(configFile, identity, 1024, "127.0.0.1", 9000, "00".repeat(32), directory);

        PoolConfig config = PoolConfig.load(configFile);
        assertEquals("127.0.0.1", config.serverHost());
        assertEquals(9000, config.serverPort());
        assertEquals(directory, config.filesDirectory());
    }

    @Test
    void filesCommandListsOnlyRegularFilesInConfiguredDirectory() throws Exception {
        var directory = Files.createTempDirectory("p2pft-files");
        Files.writeString(directory.resolve("alpha.txt"), "alpha");
        Files.createDirectory(directory.resolve("nested"));
        var config = new PoolConfig("pool", PoolIdentity.generate("pool"), 1024, "localhost", 9000,
                "00".repeat(32), directory);

        assertEquals(java.util.List.of(directory.resolve("alpha.txt")), P2PClientCli.listFiles(config));
    }
}
