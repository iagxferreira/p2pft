package com.p2pft.protocol;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;

public final class Chunker {
    private Chunker() {}

    public static List<Chunk> split(byte[] data, int chunkSize) {
        if (data == null || chunkSize <= 0) throw new IllegalArgumentException("invalid chunk size");
        var chunks = new java.util.ArrayList<Chunk>();
        for (int offset = 0, index = 0; offset < data.length; offset += chunkSize, index++) {
            chunks.add(new Chunk(index, java.util.Arrays.copyOfRange(data, offset, Math.min(offset + chunkSize, data.length))));
        }
        return List.copyOf(chunks);
    }

    public static byte[] join(List<Chunk> input) {
        var chunks = input.stream().sorted(Comparator.comparingInt(Chunk::index)).toList();
        var output = new ByteArrayOutputStream();
        for (int expected = 0; expected < chunks.size(); expected++) {
            Chunk chunk = chunks.get(expected);
            if (chunk.index() != expected) throw new IntegrityException("missing or duplicate chunk " + expected);
            output.writeBytes(chunk.data());
        }
        return output.toByteArray();
    }
}
