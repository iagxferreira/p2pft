package com.p2pft.server;

import com.p2pft.protocol.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public final class P2PServer {
    private static final PeerIdentity IDENTITY = PeerIdentity.generate();
    private static final TransferLogger LOGGER = TransferLogger.stdout();
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("usage: P2PServer <port> <output-directory> <32-byte-key-hex>");
        serve(Integer.parseInt(args[0]), Path.of(args[1]), Hashing.fromHex(args[2]));
    }

    public static void serve(int port, Path outputDirectory, byte[] key) throws IOException {
        Files.createDirectories(outputDirectory);
        Discovery.announce(IDENTITY, port, LOGGER);
        try (var server = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                var socket = server.accept();
                Thread.startVirtualThread(() -> receive(socket, outputDirectory, key));
            }
        }
    }

    private static void receive(java.net.Socket socket, Path outputDirectory, byte[] key) {
        try (socket) {
            TransferManifest manifest = TransferManifest.read(new ByteArrayInputStream(FrameCodec.read(socket.getInputStream())));
            LOGGER.connected(manifest.senderPeerId(), socket.getRemoteSocketAddress().toString());
            LOGGER.split(manifest.fileName(), manifest.chunkCount(), manifest.chunkSize());
            var chunks = new ArrayList<Chunk>();
            for (int index = 0; index < manifest.chunkCount(); index++) {
                byte[] associatedData = (manifest.fileId() + ":" + index).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                chunks.add(new Chunk(index, CryptoBox.decrypt(key, FrameCodec.read(socket.getInputStream()), associatedData)));
            }
            byte[] content = Chunker.join(chunks);
            if (content.length != manifest.size() || !Hashing.hex(Hashing.sha256(content)).equals(manifest.fileHash())) {
                throw new IntegrityException("manifest digest mismatch");
            }
            Files.write(outputDirectory.resolve(Path.of(manifest.fileName()).getFileName()), content);
            LOGGER.received(manifest.senderPeerId(), manifest.fileName());
            LOGGER.verified(manifest.fileName(), manifest.fileHash());
            FrameCodec.write(socket.getOutputStream(), "OK".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception failure) {
            LOGGER.rejected(socket.getRemoteSocketAddress().toString(), failure.getMessage());
            try { FrameCodec.write(socket.getOutputStream(), ("REJECTED: " + failure.getMessage()).getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
            catch (IOException ignored) { }
        }
    }
}
