package com.thisispmb.bushraat.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Env {
    private static final Map<String, String> FILE_VALUES = loadFile();

    private Env() {
    }

    public static String get(String key) {
        String value = System.getProperty(key);

        if (isPresent(value)) {
            return value.trim();
        }

        value = System.getenv(key);

        if (isPresent(value)) {
            return value.trim();
        }

        value = FILE_VALUES.get(key);

        return isPresent(value) ? value.trim() : null;
    }

    public static String get(String key, String fallback) {
        String value = get(key);
        return value == null ? fallback : value;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static Map<String, String> loadFile() {
        Map<String, String> values = new HashMap<>();

        Path file = Path.of(".env");

        if (!Files.isRegularFile(file)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separator = trimmed.indexOf('=');

                if (separator <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {

                    value = value.substring(1, value.length() - 1);
                }

                values.put(key, value);
            }
            
        } catch (IOException e) {
            System.err.println("Warning: Could not read .env file: " + e.getMessage());
        }

        return values;
    }
}