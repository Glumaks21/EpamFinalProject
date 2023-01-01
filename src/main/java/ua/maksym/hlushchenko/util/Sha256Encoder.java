package ua.maksym.hlushchenko.util;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class Sha256Encoder {
    public static String encode(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte aByte : hash) {
                String hex = Integer.toHexString(0xff & aByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
