package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class Peer {

    /*
     * Common Configuration Information
     */
    // The number of neighbors which are preferred
    private int numberPreferredNeighbors;
    // Unchoking Interval
    private int unchoke;
    // Optimistic Unchoking Interval
    private int optimisticUnchoke;
    // Name of the file desired by the peer
    private String filename;
    // Size of the desired file in bytes
    private int filesize;
    // Size of a piece in bytes
    private int piecesize;

    // Peers of the current peers
    private ArrayList<PeerConfiguration> peers;

    public void Peer(String commonConfigPath, String peerConfigPath) throws FileNotFoundException, ParseException, IOException {
        CommonConfiguration commonConfig = new CommonConfiguration(commonConfigPath);
        commonConfig.load(); // Let it throw
        this.numberPreferredNeighbors = commonConfig.numberPreferredNeighbors;
        this.unchoke = commonConfig.unchokingInterval;
        this.optimisticUnchoke = commonConfig.optimisticUnchokingInterval;
        this.filename = commonConfig.filename;
        this.filesize = commonConfig.filesize;
        this.piecesize = commonConfig.piecesize;
        this.peers = PeerConfiguration.loadPeerConfigurations(peerConfigPath); // Let it throw
    }
}
