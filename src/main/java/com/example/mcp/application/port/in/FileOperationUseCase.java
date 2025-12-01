package com.example.mcp.application.port.in;

import java.util.List;

import com.example.mcp.domain.model.FileMetadata;
import com.example.mcp.domain.valueobject.FilePath;

/**
 * Input port (use case interface) for file system operations.
 * This defines what the application can do with files.
 */
public interface FileOperationUseCase {
    
    /**
     * Reads the contents of a text file.
     * 
     * @param path The file path
     * @return The file contents as a string
     * @throws FileOperationException if the file cannot be read
     */
    String readFile(FilePath path) throws FileOperationException;
    
    /**
     * Writes content to a file (creates or overwrites).
     * 
     * @param path The file path
     * @param content The content to write
     * @throws FileOperationException if the file cannot be written
     */
    void writeFile(FilePath path, String content) throws FileOperationException;
    
    /**
     * Lists all files and directories in a directory.
     * 
     * @param path The directory path (null or empty for current directory)
     * @return List of file metadata
     * @throws FileOperationException if the directory cannot be listed
     */
    List<FileMetadata> listDirectory(FilePath path) throws FileOperationException;
    
    /**
     * Exception thrown when file operations encounter an error.
     */
    class FileOperationException extends Exception {
        public FileOperationException(String message) {
            super(message);
        }
        
        public FileOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
