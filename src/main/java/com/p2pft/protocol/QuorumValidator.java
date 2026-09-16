package com.p2pft.protocol;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class QuorumValidator {
    private QuorumValidator() {}

    public static boolean hasQuorum(List<PeerReport> reports, int totalPeers) {
        if (totalPeers <= 0 || reports == null || reports.size() > totalPeers) return false;
        int required = totalPeers / 2 + 1;
        Map<String, Long> counts = reports.stream().collect(Collectors.groupingBy(PeerReport::digest, Collectors.counting()));
        return counts.values().stream().anyMatch(count -> count >= required);
    }
}
