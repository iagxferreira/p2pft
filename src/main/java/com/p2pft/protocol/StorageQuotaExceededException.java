package com.p2pft.protocol;

import java.io.IOException;

public final class StorageQuotaExceededException extends IOException {
    public StorageQuotaExceededException(long requested, long available) {
        super("storage contribution exceeded: requested=" + requested + " available=" + available);
    }
}
