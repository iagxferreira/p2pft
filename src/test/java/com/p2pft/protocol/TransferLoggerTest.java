package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferLoggerTest {
    @Test
    void logsPeerConnectionSplittingAndBlobProgress() {
        var output = new ByteArrayOutputStream();
        var logger = new TransferLogger(new PrintStream(output));

        logger.connected("peer-a", "127.0.0.1:9000");
        logger.split("photo.bin", 4, 65536);
        logger.blobSent("peer-a", 2, 4);

        String log = output.toString();
        assertTrue(log.contains("event=peer_connected peer=peer-a address=127.0.0.1:9000"));
        assertTrue(log.contains("event=file_split file=photo.bin blobs=4 chunk_size=65536"));
        assertTrue(log.contains("event=blob_sent peer=peer-a blob=2 total=4"));
    }
}
