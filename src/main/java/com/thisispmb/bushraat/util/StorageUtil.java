package com.thisispmb.bushraat.util;

import java.nio.file.Files;
import java.nio.file.Path;

public final class StorageUtil {
    private StorageUtil() {
    }

    public static Path root() {
        Path root = Path.of("C:\\Users\\App\\IdeaProjects\\bushraat\\uploads")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create upload directory.", e);
        }

        return root;
    }

    public static Path books() {
        return ensure(root().resolve("books"));
    }

    public static Path covers() {
        return ensure(root().resolve("covers"));
    }

    private static Path ensure(Path path) {
        try {
            Files.createDirectories(path);
            return path;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create storage directory.", e);
        }
    }

    public static Path resolveStoredPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }

        String normalized = storedPath.replace('\\', '/');
        int marker = normalized.indexOf("/uploads/");

        if (marker >= 0) {
            normalized = normalized.substring(marker + "/uploads/".length());
        }

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        Path resolved = root().resolve(normalized).normalize();

        if (!resolved.startsWith(root())) {
            throw new SecurityException("Invalid storage path.");
        }

        return resolved;
    }
}