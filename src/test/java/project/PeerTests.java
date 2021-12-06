package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PeerTests {

    @Test
    void testPickOptUnchokedNeighbor() {
        ArrayList<PeerConfiguration> peers = new ArrayList<>();
        peers.addAll(Arrays.asList(
            new PeerConfiguration(1,"foo",8000,false),
            new PeerConfiguration(2,"foo",8000,false),
            new PeerConfiguration(3,"foo",8000,false),
            new PeerConfiguration(4,"foo",8000,false)
        ));
        Map<Integer,Boolean> preferred = new Hashtable<>();
        Map<Integer,Boolean> interested = new Hashtable<>();
        preferred.put(1, false);
        interested.put(1, false);
        preferred.put(2, false);
        interested.put(2, true);
        preferred.put(3, true);
        interested.put(3, false);
        preferred.put(4, true);
        interested.put(4, true);

        Assertions.assertEquals(Peer.pickOptUnchokedNeighbor(peers, preferred, interested), 2);
    }

    @Test
    void testComputePreferredNeighbors() {
        ArrayList<PeerConfiguration> peers = new ArrayList<>(Arrays.asList(
            new PeerConfiguration(1,"foo",8000,false),
            new PeerConfiguration(2,"foo",8000,false),
            new PeerConfiguration(3,"foo",8000,false),
            new PeerConfiguration(4,"foo",8000,false)
        ));
        ConcurrentMap<Integer, Integer> scores = new ConcurrentHashMap<>();
        scores.put(1,10);
        scores.put(2,5);
        scores.put(3,0);
        scores.put(4,5);

        ConcurrentMap<Integer,Boolean> result = Peer.computePreferredNeighbors(peers, scores, 1);
        Assertions.assertTrue(result.get(1));
        Assertions.assertFalse(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(4));

        result = Peer.computePreferredNeighbors(peers, scores, 2);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2) ^ result.get(4));
        Assertions.assertFalse(result.get(3));

        result = Peer.computePreferredNeighbors(peers, scores, 3);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertTrue(result.get(4));

        result = Peer.computePreferredNeighbors(peers, scores, 4);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertTrue(result.get(3));
        Assertions.assertTrue(result.get(4));
    }
}
