package project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import project.CommonConfiguration;

import java.io.FileNotFoundException;

public class CommonConfigurationTests {

    private static final String COMMON_CONFIG_PATH_SMALL = ".\\project_config_file_small\\project_config_file_small\\Common.cfg";

    private CommonConfiguration getCommonConfiguration() {
        return new CommonConfiguration(COMMON_CONFIG_PATH_SMALL);
    }

    @Test
    void testLoad() {
        CommonConfiguration config = getCommonConfiguration();
        try{
            config.load();
        }
        catch (Exception e) {
            Assertions.assertTrue(false); // Fail if it throws exception
        }
        Assertions.assertEquals(config.numberPreferredNeighbors, 3);
        Assertions.assertEquals(config.unchokingInterval, 5);
        Assertions.assertEquals(config.optimisticUnchokingInterval, 10);
        Assertions.assertEquals(config.filesize, 2167705);
        Assertions.assertEquals(config.piecesize, 16384);
        Assertions.assertTrue(config.filename.equals("thefile"));
    }

    @Test
    void testInvalidFilePath() {
        CommonConfiguration config = new CommonConfiguration(".\\invalid_path\\Common.cfg");
        Exception e = Assertions.assertThrows(FileNotFoundException.class,
                () -> { config.load(); });

        Assertions.assertTrue(e.getMessage().contains("Could not find Common Configuration file at"));
    }
}
