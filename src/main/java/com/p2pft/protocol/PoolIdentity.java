package com.p2pft.protocol;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PoolIdentity {
    private final String pool;
    private final KeyPair keys;

    private PoolIdentity(String pool, KeyPair keys) {
        if (pool == null || pool.isBlank()) throw new IllegalArgumentException("pool is required");
        this.pool = pool;
        this.keys = keys;
    }

    public static PoolIdentity generate(String pool) {
        try {
            var generator = KeyPairGenerator.getInstance("Ed25519");
            return new PoolIdentity(pool, generator.generateKeyPair());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Ed25519 is unavailable", e);
        }
    }

    static PoolIdentity fromEncoded(String pool, String publicKey, String privateKey) {
        try {
            var factory = KeyFactory.getInstance("Ed25519");
            PublicKey publicPart = factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
            PrivateKey privatePart = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
            return new PoolIdentity(pool, new KeyPair(publicPart, privatePart));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid Ed25519 identity", e);
        }
    }

    public String pool() { return pool; }
    public String peerId() { return Hashing.hex(Hashing.sha256(keys.getPublic().getEncoded())); }
    public String publicKey() { return Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()); }
    public String privateKey() { return Base64.getEncoder().encodeToString(keys.getPrivate().getEncoded()); }

    public byte[] sign(byte[] data) {
        try {
            var signature = Signature.getInstance("Ed25519");
            signature.initSign(keys.getPrivate());
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("unable to sign pool proof", e);
        }
    }

    public boolean verify(byte[] data, byte[] signatureBytes) {
        try {
            var signature = Signature.getInstance("Ed25519");
            signature.initVerify(keys.getPublic());
            signature.update(data);
            return signature.verify(signatureBytes);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
