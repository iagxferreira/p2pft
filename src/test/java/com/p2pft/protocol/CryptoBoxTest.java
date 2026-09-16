package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class CryptoBoxTest {
    @Test
    void encryptedPayloadRoundTripsAndTamperingIsRejected() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        byte[] plaintext = "private peer payload".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = CryptoBox.encrypt(key, plaintext, "chunk-0".getBytes(StandardCharsets.UTF_8));

        assertFalse(java.util.Arrays.equals(plaintext, encrypted));
        assertArrayEquals(plaintext, CryptoBox.decrypt(key, encrypted, "chunk-0".getBytes(StandardCharsets.UTF_8)));
        encrypted[encrypted.length - 1] ^= 1;
        assertThrows(SecurityException.class,
                () -> CryptoBox.decrypt(key, encrypted, "chunk-0".getBytes(StandardCharsets.UTF_8)));
    }
}
