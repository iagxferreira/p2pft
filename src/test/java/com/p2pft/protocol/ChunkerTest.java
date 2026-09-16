package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkerTest {
    @Test
    void splitsAndReassemblesDataWithIndexedHashes() {
        byte[] source = "a distributed file is a set of verifiable chunks".getBytes(StandardCharsets.UTF_8);

        List<Chunk> chunks = Chunker.split(source, 9);

        assertEquals(6, chunks.size());
        assertEquals(0, chunks.getFirst().index());
        assertArrayEquals(source, Chunker.join(chunks));
        assertTrue(chunks.stream().allMatch(chunk -> chunk.hash().length == 32));
    }

    @Test
    void rejectsMissingChunkWhenJoining() {
        List<Chunk> chunks = new java.util.ArrayList<>(Chunker.split(new byte[]{1, 2, 3, 4}, 2));
        chunks.set(1, new Chunk(2, new byte[]{3, 4}));

        assertThrows(IntegrityException.class, () -> Chunker.join(chunks));
    }
}
