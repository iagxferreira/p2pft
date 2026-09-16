package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscoveryTest {
    @Test
    void encodesAndDecodesPeerAnnouncements() {
        String message = Discovery.encodeAnnouncement("abc123", 9000);

        PeerAnnouncement announcement = Discovery.decodeAnnouncement("192.168.1.10", message);

        assertEquals(new PeerAnnouncement("abc123", "192.168.1.10", 9000), announcement);
    }
}
