package project;

import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class MessageLogger {

    private Logger log;

    public MessageLogger(int id) {
        this.log = Logger.getLogger("log_peer_" + id);
    }

    public void logTCP (int selfID, int targetID) {
        this.log.info("Peer " + selfID + " makes a connection to Peer " + targetID + ".");
    }

    public void logChangeNeighbors (int selfID, ConcurrentMap<Integer,Boolean> neighbors) {
        String neighborList = null;
        Boolean first = true;
        for (ConcurrentMap.Entry<Integer,Boolean> entry : neighbors.entrySet()) {
            if(!first) {
                neighborList.concat(", ");
            }
            else {
                first = false;
            }
            neighborList.concat(entry.getKey().toString());
        }

        this.log.info("Peer " + selfID + " has the preferred neighbors" + neighborList + ".");
    }

    public void logOptimistic (int selfID, int unchokedID) {
        log.info("Peer " + selfID + " has the optimistically unchoked neighbor " + unchokedID + ".");
    }

    public void logUnchoked (int selfID, int unchokedID) {
        log.info("Peer " + selfID + " is unchoked by " + unchokedID + ".");
    }
}

