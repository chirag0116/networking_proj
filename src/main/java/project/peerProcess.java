package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

/**
 * This class holds only the main function for the Peer
 * class and implements the command-line interface described in the spec
 */
public class peerProcess {

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
        catch (IllegalArgumentException e) {
            System.out.println("Could not create Peer due to illegal argument");
            e.printStackTrace();
        }

        if (peer == null) {
            return; // Fail
        }

        peer.run();
    }

}
