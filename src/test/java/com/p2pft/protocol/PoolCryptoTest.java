package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PoolCryptoTest {
    @Test
    void poolStoresEncryptedBlobsThatRequireTheClientKeyToRebuild() {
        byte[] plaintext = "secret file content".getBytes(StandardCharsets.UTF_8);
        FileCiphertext encrypted = FileCiphertext.encrypt(plaintext, 4);

        assertFalse(Arrays.equals(plaintext, encrypted.blobs().getFirst().ciphertext()));
        assertArrayEquals(plaintext, encrypted.decrypt());
        assertThrows(SecurityException.class, () -> encrypted.decryptWith(new byte[32]));
    }
}
