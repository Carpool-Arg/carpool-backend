package com.carpool.carpool.security.utils;

import java.security.SecureRandom;
import java.util.Random;

public class UserUtils {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_USERNAME_LENGTH = 25;
    private static final String PREFIX = "g_";
    private static final int RANDOM_PART_LENGTH = MAX_USERNAME_LENGTH - PREFIX.length();

    private static final Random random = new SecureRandom();

    public static String generateRandomUsername() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
