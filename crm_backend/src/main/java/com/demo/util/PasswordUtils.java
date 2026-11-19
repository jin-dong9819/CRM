package com.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER =
            new BCryptPasswordEncoder(12);

    private PasswordUtils() {
    }

    /**
     * 生成密码哈希，保存到数据库的 password_hash 字段。
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与数据库中的密码哈希是否匹配。
     */
    public static boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null
                || passwordHash == null
                || passwordHash.isEmpty()) {
            return false;
        }

        return ENCODER.matches(rawPassword, passwordHash);
    }

}