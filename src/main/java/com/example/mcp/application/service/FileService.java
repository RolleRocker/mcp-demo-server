package com.example.mcp.application.service;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import com.example.mcp.application.port.in.FileOperationUseCase;
import com.example.mcp.application.port.out.FileSystemPort;
import com.example.mcp.application.port.out.LoggingPort;
import com.example.mcp.domain.model.FileMetadata;
import com.example.mcp.domain.service.FilePathValidator;
import com.example.mcp.domain.valueobject.FilePath;

/**
 * Application service implementing file operation use case.
 * Contains pure business logic with no infrastructure dependencies.
 */
public final class FileService implements FileOperationUseCase {
    private final FileSystemPort fileSystem;
    private final LoggingPort logger;

    public FileService(FileSystemPort fileSystem, LoggingPort logger) {
        this.fileSystem = fileSystem;
        this.logger = logger;
    }

    @Override
    public String readFile(FilePath path) throws FileOperationException {
        logger.info("Reading file: " + path);
        
        // Validate path using domain service
        FilePathValidator.validateSafePath(path);
        
        try {
            if (!fileSystem.exists(path)) {
                throw new FileOperationException("File not found: " + path);
            }
            
            if (!fileSystem.isRegularFile(path)) {
                throw new FileOperationException("Not a regular file: " + path);
            }
            
            byte[] bytes = fileSystem.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            
            logger.info("Successfully read " + bytes.length + " bytes from: " + path);
            return content;
            
        } catch (FileSystemPort.FileSystemException e) {
            logger.error("Error reading file: " + e.getMessage());
            throw new FileOperationException("Error reading file: " + path, e);
        }
    }

    @Override
    public void writeFile(FilePath path, String content) throws FileOperationException {
        logger.info("Writing to file: " + path + " (" + content.length() + " bytes)");
        
        // Validate path using domain service
        FilePathValidator.validateSafePath(path);
        
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            fileSystem.writeAllBytes(path, bytes);
            
            logger.info("Successfully wrote " + bytes.length + " bytes to: " + path);
            
        } catch (FileSystemPort.FileSystemException e) {
            logger.error("Error writing file: " + e.getMessage());
            throw new FileOperationException("Error writing file: " + path, e);
        }
    }

    @Override
    public List<FileMetadata> listDirectory(FilePath path) throws FileOperationException {
        // Use current directory if path is null
        FilePath dirPath = path != null ? path : new FilePath(".");
        
        logger.info("Listing directory: " + dirPath);
        
        try {
            if (!fileSystem.exists(dirPath)) {
                throw new FileOperationException("Directory not found: " + dirPath);
            }
            
            if (!fileSystem.isDirectory(dirPath)) {
                throw new FileOperationException("Not a directory: " + dirPath);
            }
            
            List<FileMetadata> entries = fileSystem.listDirectory(dirPath);
            entries.sort(Comparator.comparing(FileMetadata::getName));
            
            logger.info("Found " + entries.size() + " entries in: " + dirPath);
            return entries;
            
        } catch (FileSystemPort.FileSystemException e) {
            logger.error("Error listing directory: " + e.getMessage());
            throw new FileOperationException("Error listing directory: " + dirPath, e);
        }
    }
}
