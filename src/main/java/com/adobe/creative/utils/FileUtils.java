package com.adobe.creative.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Ensures a directory exists, creating it if necessary
     */
    public static void ensureDirectoryExists(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                logger.debug("Created directory: {}", dirPath);
            } catch (IOException e) {
                logger.error("Failed to create directory: {}", dirPath, e);
            }
        }
    }

    /**
     * Checks if a file exists
     */
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * Sanitizes a filename by removing invalid characters
     */
    public static String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_").toLowerCase();
    }
}
