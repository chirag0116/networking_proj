package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

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
}
