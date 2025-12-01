package com.example.mcp.application.port.in;

import java.util.List;

/**
 * Input port (use case interface) for querying MCP resources.
 * This defines what the application can do with resources.
 */
public interface ResourceQueryUseCase {
    
    /**
     * Represents an MCP resource.
     */
    record Resource(
        String uri,
        String mimeType,
        String name,
        String description
    ) {}
    
    /**
     * Represents resource content.
     */
    record ResourceContent(
        String uri,
        String mimeType,
        String text
    ) {}
    
    /**
     * Lists all available resources.
     * 
     * @return List of available resources
     */
    List<Resource> listResources();
    
    /**
     * Reads the content of a specific resource.
     * 
     * @param uri The resource URI
     * @return The resource content
     * @throws IllegalArgumentException if the resource is not found
     */
    ResourceContent readResource(String uri);
}
