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
            new PeerConfiguration(4,"foo",8000,false),
            new PeerConfiguration(5,"foo",8000,false)
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
        preferred.put(5, false);
        interested.put(5, true);

        Assertions.assertEquals(Peer.pickOptUnchokedNeighbor(peers, preferred, interested, 5), 2);
    }

    @Test
    void testComputePreferredNeighbors() {
        ArrayList<PeerConfiguration> peers = new ArrayList<>(Arrays.asList(
            new PeerConfiguration(1,"foo",8000,false),
            new PeerConfiguration(2,"foo",8000,false),
            new PeerConfiguration(3,"foo",8000,false),
            new PeerConfiguration(4,"foo",8000,false),
            new PeerConfiguration(5,"foo",8000,false)
        ));
        ConcurrentMap<Integer, Integer> scores = new ConcurrentHashMap<>();
        scores.put(1,10);
        scores.put(2,5);
        scores.put(3,0);
        scores.put(4,5);
        scores.put(5,25);

        ConcurrentMap<Integer, Boolean> interested = new ConcurrentHashMap<>();
        interested.put(1,true);
        interested.put(2,true);
        interested.put(3,false);
        interested.put(4,true);
        interested.put(5,false);

        ConcurrentMap<Integer,Boolean> result = Peer.computePreferredNeighbors(peers, scores, interested, 1);
        Assertions.assertTrue(result.get(1));
        Assertions.assertFalse(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(4));
        Assertions.assertFalse(result.get(5));

        result = Peer.computePreferredNeighbors(peers, scores, interested, 2);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2) ^ result.get(4));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(5));

        result = Peer.computePreferredNeighbors(peers, scores, interested, 3);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertTrue(result.get(4));
        Assertions.assertFalse(result.get(5));

        result = Peer.computePreferredNeighbors(peers, scores, interested, 4);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertTrue(result.get(4));
        Assertions.assertFalse(result.get(5));
    }

    @Test
    void testComputePreferredNeighborsAltruistic() {
        ArrayList<PeerConfiguration> peers = new ArrayList<>(Arrays.asList(
                new PeerConfiguration(1,"foo",8000,false),
                new PeerConfiguration(2,"foo",8000,false),
                new PeerConfiguration(3,"foo",8000,false),
                new PeerConfiguration(4,"foo",8000,false)
        ));
        ConcurrentMap<Integer, Boolean> interested = new ConcurrentHashMap<>();
        interested.put(1,true);
        interested.put(2,true);
        interested.put(3,false);
        interested.put(4,false);

        ConcurrentMap<Integer,Boolean> result = Peer.computePreferredNeighborsAltruistic(peers, interested, 1);
        Assertions.assertTrue(result.get(1) ^ result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(4));

        result = Peer.computePreferredNeighborsAltruistic(peers, interested, 2);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(4));

        result = Peer.computePreferredNeighborsAltruistic(peers, interested, 3);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(4));

        result = Peer.computePreferredNeighborsAltruistic(peers, interested, 4);
        Assertions.assertTrue(result.get(1));
        Assertions.assertTrue(result.get(2));
        Assertions.assertFalse(result.get(3));
        Assertions.assertFalse(result.get(4));
    }

    // Run this from project_config_file_small/project_config_file_small working directory
    @Test
    void testHandleBitfieldMessage() {
        boolean[] senderBitfield = {true, false};
        PeerConfiguration sender = new PeerConfiguration(1,"foo",1,false);
        BitfieldMessage msg = new BitfieldMessage(senderBitfield, sender);

        boolean[] self1 = {false,false};
        boolean[] self2 = {false,true};
        boolean[] self3 = {true,false};
        boolean[] self4 = {true,true};

        Message response = Peer.handleBitfieldMessage(msg, senderBitfield, self1);
        Assertions.assertTrue(response instanceof InterestedMessage);
        Assertions.assertEquals(response.getPeer(), sender);

        response = Peer.handleBitfieldMessage(msg, senderBitfield, self2);
        Assertions.assertTrue(response instanceof InterestedMessage);
        Assertions.assertEquals(response.getPeer(), sender);

        response = Peer.handleBitfieldMessage(msg, senderBitfield, self3);
        Assertions.assertTrue(response instanceof UninterestedMessage);
        Assertions.assertEquals(response.getPeer(), sender);

        response = Peer.handleBitfieldMessage(msg, senderBitfield, self4);
        Assertions.assertTrue(response instanceof UninterestedMessage);
        Assertions.assertEquals(response.getPeer(), sender);
    }

    @Test
    void testPickNewPieceToRequest() {
        boolean[] peers = {true, false, true, false, true};
        boolean[] self = {false, true, true, false, false};
        Map<Integer,Integer> requested = new Hashtable<>();
        requested.put(3,0);
        Assertions.assertEquals(Peer.pickNewPieceToRequest(peers, self, requested), 4);
        peers[4] = false;
        Assertions.assertEquals(Peer.pickNewPieceToRequest(peers, self, requested), -1);
        requested.remove(3);
        Assertions.assertEquals(Peer.pickNewPieceToRequest(peers, self, requested), 0);
        peers[0] = false;
        Assertions.assertEquals(Peer.pickNewPieceToRequest(peers, self, requested), -1);
    }

    @Test
    void testHasAllPieces() {
        boolean[] bitfield = {false, false, false, false, false};
        for(int i = 0; i < bitfield.length; i++) {
            Assertions.assertFalse(Peer.hasAllPieces(bitfield));
            bitfield[i] = true;
        }
        Assertions.assertTrue(Peer.hasAllPieces(bitfield));
    }
}
