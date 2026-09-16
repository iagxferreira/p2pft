package com.p2pft.protocol;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

public final class CryptoBox {
    private static final int IV_SIZE = 12;
    private CryptoBox() {}

    public static byte[] encrypt(byte[] key, byte[] plaintext, byte[] associatedData) {
        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            cipher.updateAAD(associatedData);
            return concat(iv, cipher.doFinal(plaintext));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("unable to encrypt payload", e);
        }
    }

    public static byte[] decrypt(byte[] key, byte[] encrypted, byte[] associatedData) {
        if (encrypted.length < IV_SIZE) throw new SecurityException("invalid encrypted payload");
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, Arrays.copyOf(encrypted, IV_SIZE)));
            cipher.updateAAD(associatedData);
            return cipher.doFinal(Arrays.copyOfRange(encrypted, IV_SIZE, encrypted.length));
        } catch (GeneralSecurityException e) {
            throw new SecurityException("payload authentication failed", e);
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
