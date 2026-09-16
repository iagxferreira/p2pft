package com.p2pft.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PeerAssignment {
    private PeerAssignment() {}

    public static Map<String, List<Integer>> roundRobin(List<String> peerIds, int blobCount) {
        if (peerIds == null || peerIds.isEmpty() || blobCount < 0) throw new IllegalArgumentException("invalid peer assignment");
        var result = new LinkedHashMap<String, List<Integer>>();
        peerIds.forEach(peer -> result.put(peer, new ArrayList<>()));
        for (int blob = 0; blob < blobCount; blob++) result.get(peerIds.get(blob % peerIds.size())).add(blob);
        return result;
    }
}
