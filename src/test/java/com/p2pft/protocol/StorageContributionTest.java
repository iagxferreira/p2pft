package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class StorageContributionTest {
    @Test
    void poolNodeCannotStoreMoreCiphertextThanItsContribution() throws Exception {
        var encrypted = FileCiphertext.encrypt("123456789".getBytes(StandardCharsets.UTF_8), 9);
        var store = new PoolBlobStore(Files.createTempDirectory("p2pft-capacity"), 8);

        assertThrows(StorageQuotaExceededException.class, () -> store.put("file", encrypted.blobs().getFirst()));
        assertEquals(0, store.usedBytes());
    }

    @Test
    void replacingABlobDoesNotDoubleCountItsCapacity() throws Exception {
        var encrypted = FileCiphertext.encrypt("1234".getBytes(StandardCharsets.UTF_8), 4);
        var store = new PoolBlobStore(Files.createTempDirectory("p2pft-capacity"), 1024);

        store.put("file", encrypted.blobs().getFirst());
        long used = store.usedBytes();
        store.put("file", encrypted.blobs().getFirst());

        assertEquals(used, store.usedBytes());
    }
}
