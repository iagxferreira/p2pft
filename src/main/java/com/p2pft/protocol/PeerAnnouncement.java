package com.p2pft.protocol;

public record PeerAnnouncement(String peerId, String address, int tcpPort) {}
