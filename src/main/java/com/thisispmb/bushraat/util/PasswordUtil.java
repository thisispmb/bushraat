package com.thisispmb.bushraat.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String password, String passwordHash) {
        return BCrypt.checkpw(password, passwordHash);
    }
}
