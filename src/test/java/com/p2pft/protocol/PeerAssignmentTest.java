package com.p2pft.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeerAssignmentTest {
    @Test
    void distributesBlobsRoundRobinAcrossPeers() {
        var assignments = PeerAssignment.roundRobin(List.of("peer-a", "peer-b"), 5);

        assertEquals(List.of(0, 2, 4), assignments.get("peer-a"));
        assertEquals(List.of(1, 3), assignments.get("peer-b"));
    }
}
