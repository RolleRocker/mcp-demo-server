package com.example.mcp.domain.model;

import java.util.Objects;

import com.example.mcp.domain.valueobject.FilePath;
import com.example.mcp.domain.valueobject.FileSize;

/**
 * Domain entity representing file system metadata.
 */
public final class FileMetadata {
    private final FilePath path;
    private final String name;
    private final FileSize size;
    private final boolean isDirectory;

    public FileMetadata(FilePath path, String name, FileSize size, boolean isDirectory) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.size = Objects.requireNonNull(size, "Size cannot be null");
        this.isDirectory = isDirectory;
    }

    public FilePath getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public FileSize getSize() {
        return size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String formatListEntry() {
        if (isDirectory) {
            return "[DIR]  " + name;
        } else {
            return "[FILE] " + name + " (" + size.format() + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMetadata)) return false;
        FileMetadata that = (FileMetadata) o;
        return isDirectory == that.isDirectory &&
               Objects.equals(path, that.path) &&
               Objects.equals(name, that.name) &&
               Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name, size, isDirectory);
    }

    @Override
    public String toString() {
        return formatListEntry();
    }
}
