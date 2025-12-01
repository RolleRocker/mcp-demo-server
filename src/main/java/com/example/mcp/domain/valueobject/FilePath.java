package com.example.mcp.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a file system path.
 */
public final class FilePath {
    private final String value;

    public FilePath(String value) {
        Objects.requireNonNull(value, "File path cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    public String getFileName() {
        int lastSeparator = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        return lastSeparator >= 0 ? value.substring(lastSeparator + 1) : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilePath)) return false;
        FilePath filePath = (FilePath) o;
        return value.equals(filePath.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
