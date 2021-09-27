package project;

import java.io.*;
import java.text.ParseException;

public class CommonConfiguration {

    private String configFilePath;

    /* Loaded Configuration Data */
    public int numberPreferredNeighbors;
    public int unchokingInterval;
    public int optimisticUnchokingInterval;
    public String filename;
    public int filesize;
    public int piecesize;

    public CommonConfiguration(String configFilePath) {
        this.configFilePath = configFilePath;
        // Populate data with invalid initial values
        numberPreferredNeighbors = -1;
        unchokingInterval = -1;
        optimisticUnchokingInterval = -1;
        filename = "";
        filesize = -1;
        piecesize = -1;
    }

    /* Load the configuration properties */
    public void load() throws FileNotFoundException, ParseException, IOException {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(configFilePath));
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not find Common Configuration file at " + configFilePath);
        }

        String line;
        int linenum = 0;
        while ((line = br.readLine()) != null) {
            int space = line.indexOf(' ');
            if (space == -1) {
                throw new ParseException(
                    String.format("Invalid line format in common configuration file %s", configFilePath), linenum);
            }
            else {
                String propName = line.substring(0, space);
                String propValue = line.substring(space+1);
                storeProperty(propName, propValue);
            }
            linenum++;
        }
    }

    private void storeProperty(String propName, String propValue) throws NumberFormatException {
        try {
            switch(propName) {
                case "NumberOfPreferredNeighbors":
                    numberPreferredNeighbors = Integer.parseInt(propValue);
                    break;
                case "UnchokingInterval":
                    unchokingInterval = Integer.parseInt(propValue);
                    break;
                case "OptimisticUnchokingInterval":
                    optimisticUnchokingInterval = Integer.parseInt(propValue);
                    break;
                case "FileName":
                    filename = propValue;
                    break;
                case "FileSize":
                    filesize = Integer.parseInt(propValue);
                    break;
                case "PieceSize":
                    piecesize = Integer.parseInt(propValue);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Invalid property (%s) in common configuration file (%s)", propName, configFilePath));
            }
        }
        catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("Invalid value of property %s in file %s", propName, configFilePath));
        }
    }
}
