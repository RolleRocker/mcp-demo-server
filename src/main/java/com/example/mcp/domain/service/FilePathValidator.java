package com.example.mcp.domain.service;

import com.example.mcp.domain.valueobject.FilePath;

/**
 * Domain service for validating file paths.
 * This contains domain logic that doesn't belong to the FilePath value object itself.
 */
public final class FilePathValidator {

    private FilePathValidator() {
        // Utility class
    }

    /**
     * Validates that a file path doesn't contain potentially dangerous patterns.
     * 
     * @param path The file path to validate
     * @throws IllegalArgumentException if the path contains dangerous patterns
     */
    public static void validateSafePath(FilePath path) {
        String value = path.getValue();
        
        // Check for path traversal attempts
        if (value.contains("..")) {
            throw new IllegalArgumentException("Path traversal is not allowed: " + value);
        }
        
        // Check for absolute paths that might be dangerous
        // This is a simple check - in production you'd want more sophisticated validation
        if (value.startsWith("/etc") || value.startsWith("/sys") || value.startsWith("/proc")) {
            throw new IllegalArgumentException("Access to system directories is not allowed: " + value);
        }
    }

    /**
     * Checks if a file path appears to be a text file based on extension.
     * 
     * @param path The file path to check
     * @return true if the path likely points to a text file
     */
    public static boolean isTextFile(FilePath path) {
        String value = path.getValue().toLowerCase();
        return value.endsWith(".txt") ||
               value.endsWith(".md") ||
               value.endsWith(".json") ||
               value.endsWith(".xml") ||
               value.endsWith(".yml") ||
               value.endsWith(".yaml") ||
               value.endsWith(".properties") ||
               value.endsWith(".log");
    }
}
