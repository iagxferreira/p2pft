package com.p2pft.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public record PoolConfig(String pool, PoolIdentity identity) {
    public static void save(Path file, PoolIdentity identity) throws IOException {
        String yaml = "pool: " + identity.pool() + "\n"
                + "peer_id: " + identity.peerId() + "\n"
                + "public_key: " + identity.publicKey() + "\n"
                + "private_key: " + identity.privateKey() + "\n";
        Files.writeString(file, yaml);
    }

    public static PoolConfig load(Path file) throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
            int separator = line.indexOf(':');
            if (separator < 1) throw new IOException("invalid pool YAML");
            values.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
        }
        String pool = required(values, "pool");
        PoolIdentity identity = PoolIdentity.fromEncoded(pool, required(values, "public_key"), required(values, "private_key"));
        if (!identity.peerId().equals(required(values, "peer_id"))) throw new IOException("pool identity hash mismatch");
        return new PoolConfig(pool, identity);
    }

    private static String required(Map<String, String> values, String name) throws IOException {
        String value = values.get(name);
        if (value == null || value.isBlank()) throw new IOException("missing pool config field: " + name);
        return value;
    }
}
