package com.example.mcp.adapter.in.mcp.handler;

import java.util.ArrayList;
import java.util.List;

import com.example.mcp.application.port.in.ResourceQueryUseCase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * MCP protocol adapter for handling resource-related requests.
 * Translates JSON-RPC resource requests into application use case calls.
 */
public final class McpResourceHandler {
    private final Gson gson = new Gson();
    private final ResourceQueryUseCase resourceQuery;

    public McpResourceHandler(ResourceQueryUseCase resourceQuery) {
        this.resourceQuery = resourceQuery;
    }

    public JsonObject listResources() {
        List<ResourceQueryUseCase.Resource> resources = resourceQuery.listResources();
        
        JsonObject result = new JsonObject();
        List<JsonObject> resourcesJson = new ArrayList<>();
        
        for (ResourceQueryUseCase.Resource resource : resources) {
            JsonObject resJson = new JsonObject();
            resJson.addProperty("uri", resource.uri());
            resJson.addProperty("mimeType", resource.mimeType());
            resJson.addProperty("name", resource.name());
            resJson.addProperty("description", resource.description());
            resourcesJson.add(resJson);
        }
        
        result.add("resources", gson.toJsonTree(resourcesJson));
        return result;
    }

    public JsonObject readResource(JsonObject params) {
        String uri = params.get("uri").getAsString();
        
        try {
            ResourceQueryUseCase.ResourceContent content = resourceQuery.readResource(uri);
            
            JsonObject result = new JsonObject();
            List<JsonObject> contents = new ArrayList<>();
            
            JsonObject contentJson = new JsonObject();
            contentJson.addProperty("uri", content.uri());
            contentJson.addProperty("mimeType", content.mimeType());
            contentJson.addProperty("text", content.text());
            contents.add(contentJson);
            
            result.add("contents", gson.toJsonTree(contents));
            return result;
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to read resource: " + uri, e);
        }
    }
}
