package at.ac.tuwien.ase.groupphase.backend.util;


import java.security.SecureRandom;

public class RandomStringGenerator {
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateRandomString(int length) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(ALPHANUMERIC.length());
            randomString.append(ALPHANUMERIC.charAt(randomIndex));
        }
        return randomString.toString();
    }
}