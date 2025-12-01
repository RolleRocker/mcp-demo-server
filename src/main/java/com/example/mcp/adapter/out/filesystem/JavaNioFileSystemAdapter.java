package com.example.mcp.adapter.out.filesystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.example.mcp.application.port.out.FileSystemPort;
import com.example.mcp.domain.model.FileMetadata;
import com.example.mcp.domain.valueobject.FilePath;
import com.example.mcp.domain.valueobject.FileSize;

/**
 * Java NIO file system adapter.
 * Implements file system port using java.nio.file APIs.
 */
public final class JavaNioFileSystemAdapter implements FileSystemPort {

    @Override
    public boolean exists(FilePath path) {
        return Files.exists(toPath(path));
    }

    @Override
    public boolean isRegularFile(FilePath path) {
        return Files.isRegularFile(toPath(path));
    }

    @Override
    public boolean isDirectory(FilePath path) {
        return Files.isDirectory(toPath(path));
    }

    @Override
    public byte[] readAllBytes(FilePath path) throws FileSystemException {
        try {
            return Files.readAllBytes(toPath(path));
        } catch (IOException e) {
            throw new FileSystemException("Failed to read file: " + path, e);
        }
    }

    @Override
    public void writeAllBytes(FilePath path, byte[] content) throws FileSystemException {
        try {
            Path nioPath = toPath(path);
            Path parent = nioPath.getParent();
            
            // Create parent directories if they don't exist
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            Files.write(nioPath, content);
        } catch (IOException e) {
            throw new FileSystemException("Failed to write file: " + path, e);
        }
    }

    @Override
    public List<FileMetadata> listDirectory(FilePath path) throws FileSystemException {
        try {
            List<FileMetadata> results = new ArrayList<>();
            Path nioPath = toPath(path);
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(nioPath)) {
                for (Path entry : stream) {
                    String name = entry.getFileName().toString();
                    boolean isDir = Files.isDirectory(entry);
                    long size = isDir ? 0 : Files.size(entry);
                    
                    FileMetadata metadata = new FileMetadata(
                        new FilePath(entry.toString()),
                        name,
                        FileSize.ofBytes(size),
                        isDir
                    );
                    results.add(metadata);
                }
            }
            
            return results;
        } catch (IOException e) {
            throw new FileSystemException("Failed to list directory: " + path, e);
        }
    }

    @Override
    public long getFileSize(FilePath path) throws FileSystemException {
        try {
            return Files.size(toPath(path));
        } catch (IOException e) {
            throw new FileSystemException("Failed to get file size: " + path, e);
        }
    }

    private Path toPath(FilePath filePath) {
        return Paths.get(filePath.getValue());
    }
}
