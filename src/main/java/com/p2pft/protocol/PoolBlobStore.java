package com.p2pft.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Stores ciphertext only. Decryption keys never enter a pool node. */
public final class PoolBlobStore {
    private final Path root;

    public PoolBlobStore(Path root) throws IOException {
        this.root = root.toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public void put(String fileId, FileCiphertext.Blob blob) throws IOException {
        Files.createDirectories(directory(fileId));
        Files.write(path(fileId, blob.index()), blob.ciphertext());
    }

    public FileCiphertext.Blob get(String fileId, int index) throws IOException {
        byte[] ciphertext = Files.readAllBytes(path(fileId, index));
        return new FileCiphertext.Blob(index, ciphertext, Hashing.hex(Hashing.sha256(ciphertext)));
    }

    public Path path(String fileId, int index) {
        if (fileId == null || !fileId.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("invalid file id");
        if (index < 0) throw new IllegalArgumentException("invalid blob index");
        return directory(fileId).resolve(index + ".blob");
    }

    private Path directory(String fileId) { return root.resolve(fileId); }
}
