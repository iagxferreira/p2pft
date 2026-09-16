package com.p2pft.protocol;

public final class SeedPolicy {
    private SeedPolicy() {}

    public static boolean canRequest(long requestedBytes, long contributedBytes) {
        return requestedBytes >= 0 && contributedBytes >= 0 && requestedBytes <= contributedBytes;
    }
}
