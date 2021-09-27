package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PeerConfigurationTests {

    private static final String PEER_CONFIG_PATH_SMALL = ".\\project_config_file_small\\project_config_file_small\\PeerInfo.cfg";

    private ArrayList<PeerConfiguration> getPeers() {
        ArrayList<PeerConfiguration> peers = new ArrayList<PeerConfiguration>();
        try {
            peers = PeerConfiguration.loadPeerConfigurations(PEER_CONFIG_PATH_SMALL);
        }
        catch (Exception e) {
            Assertions.assertTrue(false); // Fail if it throws exception
        }
        return peers;
    }

    @Test
    void testLoadPeers() {
        ArrayList<PeerConfiguration> peers = getPeers();

        // The assertions in this loop only hold for PEER_CONFIG_PATH_SMALL
        for (int i = 0; i < peers.size(); i++) {
            PeerConfiguration p = peers.get(i);

            Assertions.assertEquals(p.getId(), 1001+i);
            String expHost = "lin114-0" + Integer.toString(i) + ".cise.ufl.edu";
            Assertions.assertTrue(p.getHostname().equals(expHost));
            Assertions.assertEquals(p.getPort(), 6001);
            if (i == 0 || i == 5) {
                Assertions.assertTrue(p.hasFile());
            }
            else {
                Assertions.assertFalse(p.hasFile());
            }
        }
    }

    @Test
    void testInvalidPath() {
        Exception e = Assertions.assertThrows(FileNotFoundException.class,
                () -> { PeerConfiguration.loadPeerConfigurations(".\\invalid_path\\PeerInfo.cfg"); });

        Assertions.assertTrue(e.getMessage().contains("Could not find Peer Configuration file at"));
    }
}
