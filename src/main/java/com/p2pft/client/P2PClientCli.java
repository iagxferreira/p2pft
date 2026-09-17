package com.p2pft.client;

import com.p2pft.protocol.Hashing;
import com.p2pft.protocol.PoolConfig;
import com.p2pft.protocol.PoolIdentity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class P2PClientCli {
    private P2PClientCli() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw usage();
        switch (args[0]) {
            case "init" -> init(args);
            case "identity" -> identity(requireConfig(args));
            case "files" -> files(requireConfig(args));
            case "upload" -> upload(requireConfig(args), requireArgument(args, 2, "file"));
            default -> throw usage();
        }
    }

    public static List<Path> listFiles(PoolConfig config) throws IOException {
        if (!Files.exists(config.filesDirectory())) return List.of();
        try (var paths = Files.list(config.filesDirectory())) {
            return paths.filter(Files::isRegularFile).sorted(Comparator.comparing(Path::toString)).toList();
        }
    }

    private static void init(String[] args) throws IOException {
        if (args.length != 8) throw usage();
        Path configFile = Path.of(args[1]);
        PoolIdentity identity = PoolIdentity.generate(args[2]);
        PoolConfig.save(configFile, identity, Long.parseLong(args[7]), args[4], Integer.parseInt(args[5]), args[6], Path.of(args[3]));
        Files.createDirectories(Path.of(args[3]));
        System.out.println("created pool config for peer " + identity.peerId());
        System.out.println("transfer key stored in config; use identity to inspect public key details");
    }

    private static void files(PoolConfig config) throws IOException {
        for (Path file : listFiles(config)) System.out.printf("%s %d bytes%n", file.getFileName(), Files.size(file));
    }

    private static void identity(PoolConfig config) {
        System.out.println("pool=" + config.pool());
        System.out.println("peer_id=" + config.identity().peerId());
        System.out.println("fingerprint=" + config.identity().sshFingerprint());
        System.out.println("public_key=" + config.identity().publicKey());
        System.out.println("contributed_bytes=" + config.contributedBytes());
        System.out.println("server=" + config.serverHost() + ":" + config.serverPort());
    }

    private static void upload(PoolConfig config, String fileArgument) throws IOException {
        if (config.transferKeyHex().isBlank()) throw new IllegalArgumentException("config has no transfer_key");
        Path file = Path.of(fileArgument);
        P2PClient.send(config.serverHost(), config.serverPort(), file, Hashing.fromHex(config.transferKeyHex()), config.identity());
        System.out.println("uploaded " + file.getFileName() + " to " + config.serverHost() + ":" + config.serverPort());
    }

    private static PoolConfig requireConfig(String[] args) throws IOException {
        return PoolConfig.load(Path.of(requireArgument(args, 1, "config")));
    }

    private static String requireArgument(String[] args, int index, String name) {
        if (args.length <= index) throw new IllegalArgumentException("missing " + name);
        return args[index];
    }

    private static IllegalArgumentException usage() {
        return new IllegalArgumentException("usage: P2PClientCli init <config> <pool> <files-dir> <host> <port> <key-hex> <contributed-bytes> | files <config> | upload <config> <file>");
    }
}
