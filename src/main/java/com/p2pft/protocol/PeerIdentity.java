package com.p2pft.protocol;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public record PeerIdentity(String value) {
    public PeerIdentity {
        if (value == null || !value.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("peer identity must be a SHA-256 hex hash");
    }

    public static PeerIdentity generate() {
        byte[] entropy = new byte[32];
        new SecureRandom().nextBytes(entropy);
        return new PeerIdentity(Hashing.hex(Hashing.sha256(entropy)));
    }

    @Override public String toString() { return value; }
}
