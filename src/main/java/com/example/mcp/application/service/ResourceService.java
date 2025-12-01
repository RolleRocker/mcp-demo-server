package com.example.mcp.application.service;

import java.util.ArrayList;
import java.util.List;

import com.example.mcp.application.port.in.ResourceQueryUseCase;
import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Application service implementing resource query use case.
 * Provides MCP resource abstraction over domain entities.
 */
public final class ResourceService implements ResourceQueryUseCase {
    private final NoteRepository noteRepository;
    private final Gson gson = new Gson();

    public ResourceService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public List<Resource> listResources() {
        List<Resource> resources = new ArrayList<>();
        
        // Static resources
        resources.add(new Resource(
            "demo://info",
            "text/plain",
            "Server Information",
            "Information about this MCP demo server"
        ));
        
        resources.add(new Resource(
            "demo://capabilities",
            "application/json",
            "MCP Capabilities",
            "Overview of MCP protocol capabilities"
        ));
        
        // Dynamic note resources
        for (Note note : noteRepository.findAll()) {
            resources.add(new Resource(
                "note://" + note.getId().getValue(),
                "text/plain",
                "Note: " + note.getTitle(),
                "Note created on " + note.getCreated()
            ));
        }
        
        return resources;
    }

    @Override
    public ResourceContent readResource(String uri) {
        if ("demo://info".equals(uri)) {
            String info = "MCP Demo Server v1.0.0\n\n" +
                         "This server demonstrates the core capabilities of the Model Context Protocol.";
            return new ResourceContent(uri, "text/plain", info);
        }
        
        if ("demo://capabilities".equals(uri)) {
            JsonObject capabilities = new JsonObject();
            capabilities.addProperty("protocol", "Model Context Protocol (MCP)");
            capabilities.addProperty("version", "1.0.0");
            
            JsonObject features = new JsonObject();
            features.addProperty("tools", "Execute functions with structured input/output");
            features.addProperty("resources", "Access and read external data sources");
            features.addProperty("prompts", "Use pre-configured prompt templates");
            capabilities.add("features", features);
            
            capabilities.addProperty("transport", "stdio");
            capabilities.addProperty("documentation", "https://modelcontextprotocol.io");
            
            return new ResourceContent(uri, "application/json", gson.toJson(capabilities));
        }
        
        if (uri.startsWith("note://")) {
            int id = Integer.parseInt(uri.substring(7));
            Note note = noteRepository.findById(new NoteId(id))
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
            
            String noteText = String.format("Title: %s\nCreated: %s\n\n%s",
                note.getTitle(), note.getCreated(), note.getContent());
            
            return new ResourceContent(uri, "text/plain", noteText);
        }
        
        throw new IllegalArgumentException("Unknown resource: " + uri);
    }
}
