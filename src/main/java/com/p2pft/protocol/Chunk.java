package com.p2pft.protocol;

import java.security.MessageDigest;
import java.util.Arrays;

public record Chunk(int index, byte[] data) {
    public Chunk {
        if (index < 0 || data == null) throw new IllegalArgumentException("invalid chunk");
        data = data.clone();
    }

    public byte[] hash() {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    @Override public byte[] data() { return data.clone(); }
    @Override public boolean equals(Object other) {
        return other instanceof Chunk c && index == c.index && Arrays.equals(data, c.data);
    }
    @Override public int hashCode() { return 31 * index + Arrays.hashCode(data); }
}
