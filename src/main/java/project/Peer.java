package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public class Peer {

    /* The number of neighbors which are preferred */
    private int numberPreferredNeighbors;
    /* Unchoking Interval */
    private int unchoke;
    /* Optimistic Unchoking Interval */
    private int optimisticUnchoke;
    /* Name of the file desired by the peer */
    private String filename;
    /* Size of the desired file in bytes */
    private int filesize;
    /* Size of a piece in bytes */
    private int piecesize;


    public void Peer(String commonConfigPath, String peerConfigPath) throws FileNotFoundException, ParseException, IOException {
        CommonConfiguration commonConfig = new CommonConfiguration(commonConfigPath);
        commonConfig.load(); // Let it throw
        numberPreferredNeighbors = commonConfig.numberPreferredNeighbors;
        unchoke = commonConfig.unchokingInterval;
        optimisticUnchoke = commonConfig.optimisticUnchokingInterval;
        filename = commonConfig.filename;
        filesize = commonConfig.filesize;
        piecesize = commonConfig.piecesize;
        // TODO -- Add peer configuration loading
    }
}
