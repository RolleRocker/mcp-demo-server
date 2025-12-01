package com.example.mcp.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a file size in bytes.
 */
public final class FileSize {
    private final long bytes;

    private FileSize(long bytes) {
        this.bytes = bytes;
    }

    public static FileSize ofBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + bytes);
        }
        return new FileSize(bytes);
    }

    public long getBytes() {
        return bytes;
    }

    public String format() {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSize)) return false;
        FileSize fileSize = (FileSize) o;
        return bytes == fileSize.bytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes);
    }

    @Override
    public String toString() {
        return format();
    }
}
