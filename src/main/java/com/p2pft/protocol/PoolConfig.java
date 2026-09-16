package com.p2pft.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public record PoolConfig(String pool, PoolIdentity identity, long contributedBytes, String serverHost,
                         int serverPort, String transferKeyHex, Path filesDirectory) {
    public static void save(Path file, PoolIdentity identity) throws IOException {
        save(file, identity, 0, "127.0.0.1", 9000, "", Path.of("files"));
    }

    public static void save(Path file, PoolIdentity identity, long contributedBytes) throws IOException {
        save(file, identity, contributedBytes, "127.0.0.1", 9000, "", Path.of("files"));
    }

    public static void save(Path file, PoolIdentity identity, long contributedBytes, String serverHost,
                            int serverPort, String transferKeyHex, Path filesDirectory) throws IOException {
        if (contributedBytes < 0) throw new IllegalArgumentException("contribution must be non-negative");
        String yaml = "pool: " + identity.pool() + "\n"
                + "peer_id: " + identity.peerId() + "\n"
                + "contributed_bytes: " + contributedBytes + "\n"
                + "server_host: " + serverHost + "\n"
                + "server_port: " + serverPort + "\n"
                + "transfer_key: " + transferKeyHex + "\n"
                + "files_directory: " + filesDirectory + "\n"
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
        long contributedBytes;
        try { contributedBytes = Long.parseLong(values.getOrDefault("contributed_bytes", "0")); }
        catch (NumberFormatException error) { throw new IOException("invalid contributed_bytes", error); }
        if (contributedBytes < 0) throw new IOException("contributed_bytes cannot be negative");
        int serverPort;
        try { serverPort = Integer.parseInt(values.getOrDefault("server_port", "9000")); }
        catch (NumberFormatException error) { throw new IOException("invalid server_port", error); }
        if (serverPort < 1 || serverPort > 65535) throw new IOException("server_port out of range");
        return new PoolConfig(pool, identity, contributedBytes, values.getOrDefault("server_host", "127.0.0.1"),
                serverPort, values.getOrDefault("transfer_key", ""), Path.of(values.getOrDefault("files_directory", "files")));
    }

    private static String required(Map<String, String> values, String name) throws IOException {
        String value = values.get(name);
        if (value == null || value.isBlank()) throw new IOException("missing pool config field: " + name);
        return value;
    }
}
