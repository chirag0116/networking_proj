package project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Class for storing information of
 * other peers in the network found in
 * PeerConfiguration
 */
public class PeerConfiguration {

    private int id;
    private String hostname;
    private int port;
    private boolean hasFile;

    public PeerConfiguration(int id, String hostname, int port, boolean hasFile) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.hasFile = hasFile;
    }

    public int getId() { return id; }
    public int getPort() { return port; }
    public boolean hasFile() { return hasFile; }
    public String getHostname() { return hostname; }

    public void setId(int id) {
        this.id = id;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public static ArrayList<PeerConfiguration> loadPeerConfigurations(String configFilePath)
        throws FileNotFoundException, IOException, ParseException {
        ArrayList<PeerConfiguration> peers = new ArrayList<PeerConfiguration>();

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(configFilePath));
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not find Peer Configuration file at " + configFilePath);
        }

        String line;
        int linenum = 0;
        while ((line = br.readLine()) != null) {
            PeerConfiguration p = parseLine(line, linenum);
            peers.add(p);
            linenum++;
        }

        return peers;
    }

    private static PeerConfiguration parseLine(String line, int linenum) throws ParseException {
        String[] fields = line.split(" ");

        if (fields.length != 4) {
            throw new ParseException(
                String.format("Invalid number of fields in peer configuration: Received %d, Expected 4", fields.length),
                linenum);
        }

        String hostname = fields[1];

        int id = -1;
        int port = -1;
        boolean hasFile = false;
        try {
            id = Integer.parseInt(fields[0]);
            port = Integer.parseInt(fields[2]);
            hasFile = (Integer.parseInt(fields[3]) == 1);
        }
        catch (NumberFormatException e) {
            throw new ParseException(
                String.format("Invalid integer value of property in peer configuration"), linenum);
        }

        return new PeerConfiguration(id, hostname, port, hasFile);
    }
}

