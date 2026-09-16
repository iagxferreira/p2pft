package com.p2pft.protocol;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class FileCiphertext {
    public record Blob(int index, byte[] ciphertext, String sha256) {
        public Blob {
            ciphertext = ciphertext.clone();
        }
        @Override public byte[] ciphertext() { return ciphertext.clone(); }
    }

    private final byte[] key;
    private final String fileId;
    private final List<Blob> blobs;

    private FileCiphertext(byte[] key, String fileId, List<Blob> blobs) {
        this.key = key.clone();
        this.fileId = fileId;
        this.blobs = List.copyOf(blobs);
    }

    public static FileCiphertext encrypt(byte[] plaintext, int blobSize) {
        if (plaintext == null || blobSize <= 0) throw new IllegalArgumentException("invalid file encryption input");
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String fileId = UUID.randomUUID().toString();
        var blobs = Chunker.split(plaintext, blobSize).stream().map(chunk -> {
            byte[] aad = (fileId + ":" + chunk.index()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] ciphertext = CryptoBox.encrypt(key, chunk.data(), aad);
            return new Blob(chunk.index(), ciphertext, Hashing.hex(Hashing.sha256(ciphertext)));
        }).toList();
        return new FileCiphertext(key, fileId, blobs);
    }

    public List<Blob> blobs() { return blobs; }
    public byte[] fileKey() { return key.clone(); }

    public byte[] decrypt() { return decryptWith(key); }

    public byte[] decryptWith(byte[] candidateKey) {
        var chunks = blobs.stream().map(blob -> {
            byte[] aad = (fileId + ":" + blob.index()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return new Chunk(blob.index(), CryptoBox.decrypt(candidateKey, blob.ciphertext(), aad));
        }).toList();
        return Chunker.join(chunks);
    }
}
