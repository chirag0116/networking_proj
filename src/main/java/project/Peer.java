package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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

    // File object for I/O from the file; not initialized until startUp() finishes
    RandomAccessFile f;

    // Peers of the current peer
    private ArrayList<PeerConfiguration> peers;

    // Message Queue
    private BlockingQueue<Message> messageQueue;

    // Servers for each peer - key=target's id, value=server
    ConcurrentHashMap<Integer,Server> servers;

    // Bitfield - stores whether each peer (including self!) has each piece
    Map<Integer, ArrayList<Boolean>> bitfields;

    // Number of pieces received in last interval from peer
    // key=peer's id, value=number of pieces
    Map<Integer, Integer> piecesReceivedInLastInterval;

    public Peer(int id, String commonConfigPath, String peerConfigPath)
            throws FileNotFoundException, ParseException, IOException, IllegalArgumentException {
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
        this.self = null;
        for (PeerConfiguration p : peersInFile) {
            if (p.getId() == id) {
                this.self = p;
            }
            else {
                this.peers.add(p);
            }

        }
        if (self == null) {
            throw new IllegalArgumentException(
                "Invalid id passed to Peer; id not found in peer config file at " + peerConfigPath);
        }

        this.messageQueue = new LinkedBlockingQueue<>();
        this.servers = new ConcurrentHashMap<>(this.peers.size()); // initial capacity
        this.bitfields = new Hashtable<>(this.peers.size() + 1); // initial capacity

        for (PeerConfiguration p : peersInFile) {
            Boolean[] bitfield = new Boolean[numberOfPieces()];
            Arrays.fill(bitfield, p.hasFile());
            this.bitfields.put(p.getId(), new ArrayList<>(Arrays.asList(bitfield)));
        }

    }

    /**
     * Attempts to push a message to the queue,
     * and blocks (waits) if the queue is currently being
     * accessed.
     * @param msg - the Message to add
     */
    public void putMessage(Message msg){
        try {
            this.messageQueue.put(msg);
        }
        catch (InterruptedException e) {
            System.out.println("Interrupted while trying to push message to Peer::messageQueue");
            e.printStackTrace();
        }
    }

    /**
     * Launch the operation of the peer.
     * Does not handle loading, but rather launches
     * processes and continually handles messages
     */
    public void run() {
        try {
            startUp();
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not open file - terminating");
            e.printStackTrace();
            return; // Terminate
        }
        catch (IOException e) {
            System.out.println("Issue setting up file - terminating");
            e.printStackTrace();
            return; // Terminate
        }

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
    public void startUp() throws FileNotFoundException, IOException {
        this.f = new RandomAccessFile(new File(getFilePath()), "rw");
        if (!self.hasFile()) {
            // Write zeros to the file
            for (int i = 0; i < numberOfPieces(); i++) {
                byte[] zeros;
                if (i == numberOfPieces() - 1) {
                    // Remaining bytes only for last piece
                    zeros = new byte[filesize - i*piecesize];
                }
                else {
                    zeros = new byte[piecesize];
                }
                Arrays.fill(zeros, (byte)0);

                f.seek((long) i * piecesize);
                f.write(zeros);
            }
        }

        for (PeerConfiguration peer : peers) {
            Peer instance = this;
            Server server = new Server(self, peer, (peer.getId() > self.getId()), (Message m) -> {
                instance.putMessage(m);
                synchronized (instance) {
                    this.notify();
                }
            });
            servers.put(peer.getId(), server);
        }

        // Putting this in a different loop because we might have to
        // do some fancy threading stuff here later
        for (PeerConfiguration peer : peers) {
            boolean success = servers.get(peer.getId()).start();
            if (!success) {
                // TODO - find a better way to handle this
                System.out.println("Server for neighbor " + peer + " failed to start");
            }
            else {
                System.out.println("Server for neighbor " + peer + " started");
            }
        }
    }

    /**
     * Shutdown all active resources,
     * such as threads, open files, etc.
     */
    public void shutDown() {
        try {
            f.close();
        }
        catch (IOException e) {
            System.out.println("File could not close properly");
        }

        for (PeerConfiguration peer : peers) {
            servers.get(peer.getId()).stop();
        }
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

    private int numberOfPieces() {
        if (filesize % piecesize == 0) {
            return filesize / piecesize;
        }
        else {
            return filesize / piecesize + 1;
        }
    }

    public String getFilePath() {
        return String.format("peer_%d/%s", self.getId(), filename);
    }

    /**
     * Load a piece from the file
     * @param index - index of the piece to load
     * @return the piece as array of bytes, or null if it could not be loaded
     * @throws IndexOutOfBoundsException if the index is too large or negative
     */
    private byte[] loadPiece(int index) throws IndexOutOfBoundsException {
        if (index > numberOfPieces() || index < 0) {
            throw new IndexOutOfBoundsException("Invalid piece index");
        }

        byte[] piece = new byte[this.piecesize];
        int numBytesRead = -1;
        try {
            f.seek((long) index * this.piecesize);
            numBytesRead = f.read(piece);
        }
        catch (IOException e) {
            System.out.println("IOException thrown while attempting to load piece with index=" + index);
            return null;
        }

        if (numBytesRead == -1) {
            return null;
        }
        else if (numBytesRead < this.piecesize) {
            byte[] pieceTrimmed = new byte[numBytesRead];
            System.arraycopy(piece, 0, pieceTrimmed, 0, numBytesRead);
            return pieceTrimmed;
        }
        else {
            return piece;
        }
    }

    private void storePiece(byte[] piece, int index) throws IndexOutOfBoundsException, IOException {
        if (index > numberOfPieces() || index < 0) {
            throw new IndexOutOfBoundsException("Invalid piece index");
        }

        f.seek(index);
        f.write(piece);
    }
}
