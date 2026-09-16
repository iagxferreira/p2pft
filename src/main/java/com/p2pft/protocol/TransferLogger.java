package com.p2pft.protocol;

import java.io.PrintStream;

public final class TransferLogger {
    private final PrintStream output;

    public TransferLogger(PrintStream output) { this.output = output; }
    public static TransferLogger stdout() { return new TransferLogger(System.out); }

    public void connected(String peer, String address) { event("peer_connected peer=" + peer + " address=" + address); }
    public void discovered(int count) { event("peers_discovered count=" + count); }
    public void discoveryBroadcast(String peer, int port) { event("discovery_broadcast peer=" + peer + " tcp_port=" + port); }
    public void seedStarted(String peer, String fingerprint, long contributedBytes, SeedLease lease) {
        event("seed_started peer=" + peer + " fingerprint=" + fingerprint + " contributed_bytes=" + contributedBytes
                + " lease_expires=" + lease.expiresAt());
    }
    public void split(String file, int blobs, int chunkSize) { event("file_split file=" + file + " blobs=" + blobs + " chunk_size=" + chunkSize); }
    public void blobSent(String peer, int blob, int total) { event("blob_sent peer=" + peer + " blob=" + blob + " total=" + total); }
    public void received(String peer, String file) { event("transfer_received peer=" + peer + " file=" + file); }
    public void verified(String file, String digest) { event("file_verified file=" + file + " sha256=" + digest); }
    public void rejected(String peer, String reason) { event("transfer_rejected peer=" + peer + " reason=" + reason); }

    private void event(String message) { output.println("event=" + message); output.flush(); }
}
