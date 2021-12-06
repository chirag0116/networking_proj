package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

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
    ConcurrentMap<Integer,Server> servers;

    // Bitfield - stores whether each peer (including self!) has each piece
    ConcurrentMap<Integer, boolean[]> bitfields;

    // Number of pieces received in last interval from peer
    // key=peer's id, value=number of pieces
    ConcurrentMap<Integer, Integer> piecesReceivedInLastInterval;

    // Stores whether a peer is currently preferred (T/F)
    // key=peer's id, value=whether peer is preferred
    ConcurrentMap<Integer, Boolean> preferred;

    // Stores whether a peer is currently interested in instance's data (received an InterestedMessage)
    // key=peer's id, value=whether peer is interested
    ConcurrentMap<Integer, Boolean> interested;

    // The peer who is optimistically unchoked right now; the integer stored is its id
    AtomicReference<Integer> optimisticallyUnchokedPeer;

    private final TimerTask DETERMINE_PREFERRED_NEIGHBORS = new TimerTask() {
        @Override
        public void run() {
            ConcurrentMap<Integer,Boolean> newPreferred;
            if (!self.hasFile()) {
                // Get the new preferred neighbors
                newPreferred = computePreferredNeighbors(
                        peers,
                        piecesReceivedInLastInterval,
                        interested,
                        numberPreferredNeighbors
                );
            }
            else {
                newPreferred = computePreferredNeighborsAltruistic(peers, interested, numberPreferredNeighbors);
            }

            // Send the choke and unchoke messages
            for (PeerConfiguration peer : peers) {
                if (newPreferred.get(peer.getId()) && !preferred.get(peer.getId())) {
                    UnchokeMessage m = new UnchokeMessage(peer);
                    servers.get(peer.getId()).sendMessage(m);
                }
                else if (!newPreferred.get(peer.getId()) && preferred.get(peer.getId())) {
                    if (optimisticallyUnchokedPeer.get() != peer.getId()) {
                        ChokeMessage m = new ChokeMessage(peer);
                        servers.get(peer.getId()).sendMessage(m);
                    }
                }
            }

            // Zero out the scores
            for (PeerConfiguration peer : peers) {
                piecesReceivedInLastInterval.put(peer.getId(), 0);
            }
        }
    };

    private final TimerTask DETERMINE_OPT_UNCHOKED_NEIGHBOR = new TimerTask() {
        @Override
        public void run() {
            int unchokeId = pickOptUnchokedNeighbor(peers, preferred, interested);
            optimisticallyUnchokedPeer.set(unchokeId);
            PeerConfiguration peer = getPeerWithId(unchokeId);
            UnchokeMessage m = new UnchokeMessage(peer);
            servers.get(unchokeId).sendMessage(m);
            System.out.println("Optimistically unchoked peer with id=" + unchokeId); // Change to proper log
        }
    };

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
        this.interested = new ConcurrentHashMap<>(this.peers.size());
        this.preferred = new ConcurrentHashMap<>(numberPreferredNeighbors);
        this.optimisticallyUnchokedPeer = new AtomicReference<>();
        this.piecesReceivedInLastInterval = new ConcurrentHashMap<>(this.peers.size());
        for (PeerConfiguration peer : peers) {
            piecesReceivedInLastInterval.put(peer.getId(), 0);
        }

        this.bitfields = new ConcurrentHashMap<>(this.peers.size() + 1); // initial capacity

        for (PeerConfiguration p : peersInFile) {
            boolean[] bitfield = new boolean[numberOfPieces()];
            Arrays.fill(bitfield, p.hasFile());
            this.bitfields.put(p.getId(), bitfield);
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

        // Delays are 1000L to convert from seconds to milliseconds
        Timer preferredTimer = new Timer();
        preferredTimer.schedule(DETERMINE_PREFERRED_NEIGHBORS, 0, unchoke * 1000L);
        Timer unchokeTimer = new Timer();
        unchokeTimer.schedule(DETERMINE_OPT_UNCHOKED_NEIGHBOR, 0, optimisticUnchoke * 1000L);

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

        boolean[] selfBitfield = bitfields.get(self.getId());
        for (PeerConfiguration peer : peers) {
            Peer instance = this;
            Server server = new Server(self, peer, (peer.getId() > self.getId()), (Message m) -> {
                instance.putMessage(m);
                synchronized (instance) {
                    this.notify();
                }
            });
            servers.put(peer.getId(), server);

            Thread serverLauncher = new Thread(() -> {
                boolean success = servers.get(peer.getId()).start();
                if (!success) {
                    // TODO - find a better way to handle this
                    System.out.println("Server for neighbor " + peer + " failed to start");
                }
                else {
                    System.out.println("Server for neighbor " + peer + " started");
                    // Send bitfield to peer if this has the file
                    if (self.hasFile()) {
                        BitfieldMessage m = new BitfieldMessage(selfBitfield, peer);
                        servers.get(peer.getId()).sendMessage(m);
                    }
                }
            });
            serverLauncher.start();
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

    /**
     * Picks a random neighbor to optimistically
     * unchoke, and returns it.
     * !!! NOTE: DOES NOT DO THE UNCHOKING, THIS MUST BE DONE
     * BY THE CALLER !!!
     * static for testing
     * @return id of the neighbor to unchoke
     */
    public static int pickOptUnchokedNeighbor(
            ArrayList<PeerConfiguration> peers,
            Map<Integer, Boolean> preferred,
            Map<Integer, Boolean> interested
    ) {
        ArrayList<PeerConfiguration> candidates = new ArrayList<>();
        for (PeerConfiguration peer : peers) {
            if (!preferred.get(peer.getId()) && interested.get(peer.getId())) {
                candidates.add(peer);
            }
        }
        Collections.shuffle(candidates);
        return candidates.get(0).getId();
    }

    /**
     * Find the peer with the specified id
     * @return the peer
     */
    private PeerConfiguration getPeerWithId(int id) throws IllegalArgumentException {
        for (PeerConfiguration peer : peers) {
            if (peer.getId() == id) {
                return peer;
            }
        }
        throw new IllegalArgumentException("No peer with specified id");
    }

    /**
     * Computes the new set of preferred neighbors,
     * and returns it
     * !!! NOTE: DOES NOT MODIFY THE CLASS MEMBERS,
     * CALLER MUST DO SO !!!
     * static for testing
     * @param peers - list of peers (just pass this.peers)
     * @param scores - map of scores for each peer from the previous interval (pass this.receivedInLastInterval)
     * @param numberNeighbors - number of preferred neighbors requested (pass this.numberOfPreferredNeighbors
     * @return map of containing id keys and value of whether peer with id is preferred
     */
    public static ConcurrentMap<Integer, Boolean> computePreferredNeighbors(
            ArrayList<PeerConfiguration> peers,
            Map<Integer, Integer> scores,
            Map<Integer, Boolean> interested,
            int numberNeighbors
    ) {
        // TODO - BUG: This needs to only include peers who are interested in the result
        ArrayList<Integer> sortedPeers = new ArrayList<>();
        for (PeerConfiguration peer : peers) {
            sortedPeers.add(peer.getId());
        }
        sortedPeers.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int score1 = scores.get(o1);
                int score2 = scores.get(o2);
                if (score1 < score2) {
                    return 1;
                } else if (score1 > score2) {
                    return -1;
                } else {
                    // Decide ties randomly, with equal probability
                    return (Math.random() > 0.5) ? 1 : -1;
                }
            }
        });
        ConcurrentMap<Integer, Boolean> result = new ConcurrentHashMap<>();
        int count = 0;
        for (Integer peer : sortedPeers) {
            if (count < numberNeighbors && interested.get(peer)) {
                result.put(peer, true);
                count++;
            }
            else {
                result.put(peer, false);
            }
        }
        return result;
    }

    /**
     * Computes a set of preferred neighbors when this instance
     * already has the file. Picks randomly from the interested peers.
     * !!! NOTE: DOES NOT MODIFY THE CLASS MEMBERS,
     * CALLER MUST DO SO !!!
     * static for testing
     * @param peers - list of peers (just pass this.peers)
     * @param interested - map of peer ids to whether they are interested
     * @param numberNeighbors - number of preferred neighbors requested (pass this.numberOfPreferredNeighbors
     * @return map of containing id keys and value of whether peer with id is preferred
     */
    public static ConcurrentMap<Integer, Boolean> computePreferredNeighborsAltruistic(
            ArrayList<PeerConfiguration> peers,
            ConcurrentMap<Integer, Boolean> interested,
            int numberNeighbors
    ) {
        ArrayList<Integer> shuffled = new ArrayList<>();
        for (PeerConfiguration peer : peers) {
            shuffled.add(peer.getId());
        }
        Collections.shuffle(shuffled);
        ConcurrentMap<Integer, Boolean> result = new ConcurrentHashMap<>();
        int count = 0;
        for (Integer peer : shuffled) {
            if (count < numberNeighbors && interested.get(peer)) {
                result.put(peer, true);
                count++;
            }
            else {
                result.put(peer, false);
            }
        }
        return result;
    }
}
