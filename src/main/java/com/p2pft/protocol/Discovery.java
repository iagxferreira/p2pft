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
        byte[] payload = (PREFIX + peerId + " " + tcpPort).getBytes(StandardCharsets.UTF_8);
        try (var socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.send(new DatagramPacket(payload, payload.length, InetAddress.getByName("255.255.255.255"), PORT));
        }
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
}
