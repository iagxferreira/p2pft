package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PoolBlobStoreTest {
    @Test
    void storesAndReturnsCiphertextWithoutADecryptOperation() throws Exception {
        var ciphertext = FileCiphertext.encrypt("private payload".getBytes(StandardCharsets.UTF_8), 4);
        var store = new PoolBlobStore(Files.createTempDirectory("p2pft-pool"));
        var blob = ciphertext.blobs().getFirst();

        store.put("file-1", blob);

        assertArrayEquals(blob.ciphertext(), store.get("file-1", blob.index()).ciphertext());
        assertFalse(new String(Files.readAllBytes(store.path("file-1", blob.index())), StandardCharsets.UTF_8).contains("private payload"));
    }
}
