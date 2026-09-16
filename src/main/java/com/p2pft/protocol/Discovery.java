package com.p2pft.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class Discovery {
    public static final int PORT = 45454;
    private static final String PREFIX = "P2PFT/1 ";
    private Discovery() {}

    public static void announce(String peerId, int tcpPort) throws IOException {
        byte[] payload = encodeAnnouncement(peerId, tcpPort).getBytes(StandardCharsets.UTF_8);
        try (var socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.send(new DatagramPacket(payload, payload.length, InetAddress.getByName("255.255.255.255"), PORT));
        }
    }

    public static String encodeAnnouncement(String peerId, int tcpPort) {
        if (peerId == null || peerId.isBlank() || tcpPort < 1 || tcpPort > 65535) throw new IllegalArgumentException("invalid peer announcement");
        return PREFIX + peerId + " " + tcpPort;
    }

    public static PeerAnnouncement decodeAnnouncement(String address, String message) {
        String[] fields = message.split(" ");
        if (fields.length != 3 || !PREFIX.trim().equals(fields[0])) throw new IllegalArgumentException("invalid discovery announcement");
        return new PeerAnnouncement(fields[1], address, Integer.parseInt(fields[2]));
    }

    public static void announce(PeerIdentity identity, int tcpPort, TransferLogger logger) throws IOException {
        announce(identity.value(), tcpPort);
        logger.discoveryBroadcast(identity.value(), tcpPort);
    }

    public static List<String> listen(Duration timeout) throws IOException {
        List<String> peers = new ArrayList<>();
        try (var socket = new DatagramSocket(PORT)) {
            socket.setSoTimeout((int) timeout.toMillis());
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    var packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                    if (message.startsWith(PREFIX)) peers.add(packet.getAddress().getHostAddress() + " " + message.substring(PREFIX.length()));
                } catch (java.net.SocketTimeoutException done) { return peers; }
            }
        }
    }

    public static List<PeerAnnouncement> listenForPeers(Duration timeout, TransferLogger logger) throws IOException {
        List<PeerAnnouncement> peers = new ArrayList<>();
        try (var socket = new DatagramSocket(PORT)) {
            socket.setSoTimeout((int) timeout.toMillis());
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    var packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                    if (message.startsWith(PREFIX)) peers.add(decodeAnnouncement(packet.getAddress().getHostAddress(), message));
                } catch (java.net.SocketTimeoutException done) {
                    logger.discovered(peers.size());
                    return peers;
                }
            }
        }
    }
}
