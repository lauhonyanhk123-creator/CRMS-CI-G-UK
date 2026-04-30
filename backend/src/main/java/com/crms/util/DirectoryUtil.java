package com.crms.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DirectoryUtil {

    private static final String UPLOAD_DIR = System.getenv().getOrDefault("UPLOAD_DIR", "/var/crms/uploads");

    private DirectoryUtil() {}

    public static Path getUploadDirectory(String... subDirs) {
        Path path = Paths.get(UPLOAD_DIR);
        for (String sub : subDirs) {
            path = path.resolve(sub);
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + path, e);
        }
        return path;
    }

    public static String getUploadDir() {
        return UPLOAD_DIR;
    }
}
