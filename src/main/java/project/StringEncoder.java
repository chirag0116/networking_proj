package project;

import java.nio.charset.StandardCharsets;

/**
 * Helper class to standardize string
 * to byte conversions across the project
 */
public class StringEncoder {

    public static final byte[] stringToBytes(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static final String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }
}
