package com.p2pft.client;

import com.p2pft.protocol.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class P2PClient {
    private static final int CHUNK_SIZE = 64 * 1024;
    private static final PeerIdentity IDENTITY = PeerIdentity.generate();
    private static final TransferLogger LOGGER = TransferLogger.stdout();

    public static void main(String[] args) throws Exception {
        if (args.length != 4) throw new IllegalArgumentException("usage: P2PClient <host> <port> <file> <32-byte-key-hex>");
        send(args[0], Integer.parseInt(args[1]), Path.of(args[2]), Hashing.fromHex(args[3]));
    }

    public static void send(String host, int port, Path file, byte[] key) throws IOException {
        byte[] content = Files.readAllBytes(file);
        String fileId = UUID.randomUUID().toString();
        String digest = Hashing.hex(Hashing.sha256(content));
        var chunks = Chunker.split(content, CHUNK_SIZE);
        LOGGER.split(file.getFileName().toString(), chunks.size(), CHUNK_SIZE);
        var manifest = new TransferManifest(IDENTITY.value(), fileId, file.getFileName().toString(), content.length, CHUNK_SIZE, chunks.size(), digest);
        try (var socket = new Socket(host, port)) {
            LOGGER.connected(IDENTITY.value(), host + ":" + port);
            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            manifest.write(encoded);
            FrameCodec.write(socket.getOutputStream(), encoded.toByteArray());
            for (Chunk chunk : chunks) {
                byte[] associatedData = (fileId + ":" + chunk.index()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                FrameCodec.write(socket.getOutputStream(), CryptoBox.encrypt(key, chunk.data(), associatedData));
                LOGGER.blobSent(host, chunk.index(), chunks.size());
            }
            if (!"OK".equals(new String(FrameCodec.read(socket.getInputStream()), java.nio.charset.StandardCharsets.UTF_8))) {
                throw new IOException("receiver rejected transfer");
            }
        }
    }
}
