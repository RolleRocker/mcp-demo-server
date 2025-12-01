package com.example.mcp.application.port.out;

import java.util.List;

import com.example.mcp.domain.model.FileMetadata;
import com.example.mcp.domain.valueobject.FilePath;

/**
 * Output port (service interface) for file system operations.
 * This defines what the application needs from a file system.
 */
public interface FileSystemPort {
    
    /**
     * Checks if a file or directory exists.
     * 
     * @param path The path to check
     * @return true if the path exists
     */
    boolean exists(FilePath path);
    
    /**
     * Checks if a path points to a regular file.
     * 
     * @param path The path to check
     * @return true if the path is a regular file
     */
    boolean isRegularFile(FilePath path);
    
    /**
     * Checks if a path points to a directory.
     * 
     * @param path The path to check
     * @return true if the path is a directory
     */
    boolean isDirectory(FilePath path);
    
    /**
     * Reads all bytes from a file.
     * 
     * @param path The file path
     * @return The file contents as a byte array
     * @throws FileSystemException if the file cannot be read
     */
    byte[] readAllBytes(FilePath path) throws FileSystemException;
    
    /**
     * Writes bytes to a file (creates or overwrites).
     * 
     * @param path The file path
     * @param content The content to write
     * @throws FileSystemException if the file cannot be written
     */
    void writeAllBytes(FilePath path, byte[] content) throws FileSystemException;
    
    /**
     * Lists all entries in a directory.
     * 
     * @param path The directory path
     * @return List of file metadata for all entries
     * @throws FileSystemException if the directory cannot be listed
     */
    List<FileMetadata> listDirectory(FilePath path) throws FileSystemException;
    
    /**
     * Gets the size of a file in bytes.
     * 
     * @param path The file path
     * @return The file size in bytes
     * @throws FileSystemException if the size cannot be determined
     */
    long getFileSize(FilePath path) throws FileSystemException;
    
    /**
     * Exception thrown when file system operations fail.
     */
    class FileSystemException extends Exception {
        public FileSystemException(String message) {
            super(message);
        }
        
        public FileSystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
