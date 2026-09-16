package com.p2pft.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Stores ciphertext only. Decryption keys never enter a pool node. */
public final class PoolBlobStore {
    private final Path root;
    private final long capacityBytes;

    public PoolBlobStore(Path root) throws IOException {
        this(root, Long.MAX_VALUE);
    }

    public PoolBlobStore(Path root, long capacityBytes) throws IOException {
        if (capacityBytes < 0) throw new IllegalArgumentException("capacity must be non-negative");
        this.root = root.toAbsolutePath().normalize();
        this.capacityBytes = capacityBytes;
        Files.createDirectories(this.root);
    }

    public void put(String fileId, FileCiphertext.Blob blob) throws IOException {
        Files.createDirectories(directory(fileId));
        long existing = Files.exists(path(fileId, blob.index())) ? Files.size(path(fileId, blob.index())) : 0;
        long projected = usedBytes() - existing + blob.ciphertext().length;
        if (projected > capacityBytes) throw new StorageQuotaExceededException(projected, capacityBytes - usedBytes());
        Files.write(path(fileId, blob.index()), blob.ciphertext());
    }

    public long capacityBytes() { return capacityBytes; }

    public long usedBytes() throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile).mapToLong(path -> {
                try { return Files.size(path); }
                catch (IOException error) { throw new StorageSizeException(error); }
            }).sum();
        } catch (StorageSizeException error) {
            throw error.cause;
        }
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

    private static final class StorageSizeException extends RuntimeException {
        private final IOException cause;
        private StorageSizeException(IOException cause) { this.cause = cause; }
    }
}
