package com.demo.util;

import java.security.SecureRandom;

public class CodeUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateCode() {
        return String.format("%04d", RANDOM.nextInt(10000));
    }

}