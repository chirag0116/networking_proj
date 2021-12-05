package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Peer {

    // The network settings of this peer
    private PeerConfiguration self;

    /*
     * Common Configuration Information
     */
    private int numberPreferredNeighbors; // The number of neighbors which are preferred
    private int unchoke; // Unchoking Interval
    private int optimisticUnchoke; // Optimistic Unchoking Interval
    private String filename; // Name of the file desired by the peer
    private int filesize; // Size of the desired file in bytes
    private int piecesize; // Size of a piece in bytes

    // Peers of the current peer
    private ArrayList<PeerConfiguration> peers;

    // Message Queue
    private BlockingQueue<Message> messageQueue;

    public Peer(int id, String commonConfigPath, String peerConfigPath) throws FileNotFoundException, ParseException, IOException {
        CommonConfiguration commonConfig = new CommonConfiguration(commonConfigPath);
        commonConfig.load(); // Let it throw
        this.numberPreferredNeighbors = commonConfig.numberPreferredNeighbors;
        this.unchoke = commonConfig.unchokingInterval;
        this.optimisticUnchoke = commonConfig.optimisticUnchokingInterval;
        this.filename = commonConfig.filename;
        this.filesize = commonConfig.filesize;
        this.piecesize = commonConfig.piecesize;

        // Split the peers from the file into this one, and the others
        ArrayList<PeerConfiguration> peersInFile = PeerConfiguration.loadPeerConfigurations(peerConfigPath); // Let it throw
        this.peers = new ArrayList<>();
        for (PeerConfiguration p : peersInFile) {
            if (p.getId() == id) {
                this.self = p;
            }
            else {
                this.peers.add(p);
            }
        }

        // Linked List based queue
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Attempts to push a message to the queue,
     * and blocks (waits) if the queue is currently being
     * accessed.
     * @param msg - the Message to add
     * @throws InterruptedException - possibly raised by putting into messageQueue
     */
    public void putMessage(Message msg) throws InterruptedException {
        this.messageQueue.put(msg);
    }

    /**
     * Launch the operation of the peer.
     * Does not handle loading, but rather launches
     * processes and continually handles messages
     */
    public void run() {
        startUp();
        while (true) {
            try {
                // Blocks until a message is available
                Message msg = this.messageQueue.take();
                handleMessage(msg);
            }
            catch (InterruptedException e) {
                reportException(e);
            }
            catch (UnsupportedOperationException e) {
                System.out.println("Hit not yet implemented code");
                reportException(e);
            }
            catch (Exception e) {
                // Unexpected exception
                reportException(e);
                System.out.println("Terminating due to unexpected exception");
                break;
            }
        }
        shutDown();
    }

    /**
     * Start all functionality not launched at
     * construction
     */
    public void startUp() {
        // TODO -- Add startup functionality
    }

    /**
     * Shutdown all active resources,
     * such as threads, open files, etc.
     */
    public void shutDown() {
        // TODO -- Add shutdown functionality
    }

    /**
     * Handle a received message.
     * Based on specification, may include a variety
     * of actions based on message type.
     * @param msg - message to be handled
     */
    private void handleMessage(Message msg) {
        // TODO - Add message handling for each message type
        throw new UnsupportedOperationException("handleMessage not yet implemented");
    }

    // TODO -- Make this dump to file and print
    private static void reportException(Exception e) {
        System.out.println("Exception caught");
        String msg = e.getMessage();
        if (msg != null) {
            System.out.println(msg);
        }
        e.printStackTrace();
    }

    public static void main(String[] args) {
        int id = -1;
        try {
            id = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid id argument - not an integer");
            return; // Fail
        }

        if (id < 0) {
            System.out.println("Invalid id argument - not a positive integer");
            return; // Fail
        }

        Peer peer = null;
        try {
            peer = new Peer(id, "Common.cfg", "PeerInfo.cfg");
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not create Peer due to missing file:");
            e.printStackTrace();
        }
        catch (ParseException e) {
            System.out.println("Could not create Peer due to invalid syntax in configuration files:");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Could not create Peer due to IOException:");
            e.printStackTrace();
        }

        if (peer == null) {
            return; // Fail
        }

        peer.startUp();
        peer.run();
        peer.shutDown();
    }
}
