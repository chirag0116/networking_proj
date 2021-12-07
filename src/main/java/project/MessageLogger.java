package project;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class MessageLogger {

    private Logger log;
    private PrintWriter f;
    private String filepath;

    public MessageLogger(int id) {
        this.log = Logger.getLogger("log_peer_" + id);
        this.filepath = "log_peer_" + id + ".log";
        try {
            FileOutputStream file = new FileOutputStream(this.filepath);
            f = new PrintWriter(file);
        }
        catch (FileNotFoundException e) {
            System.out.println("Logger could not be opened to " + this.filepath);
            f = null;
        }
    }

    public void writeMessage(String message) {
        String log = getTimeStampString() + " " + message;
        this.log.info(log);
        if (f != null) {
            f.println(log);
            f.flush();
        }
        else {
            System.out.println("Skipped writing log to " + filepath + " because PrintWriter was null");
        }
    }

    public static String getTimeStampString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    public void logTCP (int selfID, int targetID) {
        writeMessage("Peer " + selfID + " makes a connection to Peer " + targetID + ".");
    }

    public void logChangeNeighbors (int selfID, ConcurrentMap<Integer,Boolean> neighbors) {
        List<Integer> preferred = new LinkedList<>();
        for (ConcurrentMap.Entry<Integer,Boolean> entry : neighbors.entrySet()) {
            // If this peer is preferred
            if (entry.getValue()) {
                preferred.add(entry.getKey());
            }
        }

        if (preferred.isEmpty()) {
            writeMessage("Peer " + selfID + " has no preferred neighbors.");
        }
        else {
            StringBuilder neighborList = new StringBuilder();
            boolean first = true;
            for (Integer id : preferred) {
                if (first) {
                    first = false;
                }
                else {
                    neighborList.append(", ");
                }
                neighborList.append(id);
            }

            writeMessage("Peer " + selfID + " has the preferred neighbors " + neighborList + ".");
        }
    }

    public void logOptimistic (int selfID, int unchokedID) {
        writeMessage("Peer " + selfID + " has the optimistically unchoked neighbor " + unchokedID + ".");
    }

    public void logUnchoked (int selfID, int unchokedID) {
        writeMessage("Peer " + selfID + " is unchoked by " + unchokedID + ".");
    }

    public void logChoked (int selfID, int chokedID) {
        writeMessage("Peer " + selfID + "is choked by " + chokedID);
    }

    public void logInterested (int selfID, int interestedID) {
        writeMessage("Peer " + selfID + "received the ‘interested’ message from" + interestedID);
    }

    public void logNotInterested (int selfID, int notInterestedID) {
        writeMessage("Peer " + selfID + "received the ‘not interested’ message from" + notInterestedID);
    }

    public void logHaveMessage(int selfID, int senderID, int pieceIndex) {
        writeMessage("Peer " + selfID + " received the 'have' message from " + senderID + " for the piece " + pieceIndex);
    }

    public void logDownload(int selfId, int peerId, int pieceIndex, boolean[] selfBitfield) {
        int count = 0;
        for (boolean hasPiece : selfBitfield) {
            if (hasPiece) {
                count++;
            }
        }
        writeMessage("Peer " + selfId + " has downloaded the piece " + pieceIndex + " from " + peerId +
                ". Now the number of pieces it has is " + count);
    }

    public void logComplete(int selfId) {
        writeMessage("Peer " + selfId + "has downloaded the complete file");
    }
}

