package com.example.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ResourceManager {
    private final Gson gson = new Gson();
    private final Map<Integer, Note> notes;

    public ResourceManager(Map<Integer, Note> notes) {
        this.notes = notes;
    }

    public JsonObject listResources() {
        JsonObject result = new JsonObject();
        List<JsonObject> resources = new ArrayList<>();

        JsonObject infoResource = new JsonObject();
        infoResource.addProperty("uri", "demo://info");
        infoResource.addProperty("mimeType", "text/plain");
        infoResource.addProperty("name", "Server Information");
        infoResource.addProperty("description", "Information about this MCP demo server");
        resources.add(infoResource);

        JsonObject capResource = new JsonObject();
        capResource.addProperty("uri", "demo://capabilities");
        capResource.addProperty("mimeType", "application/json");
        capResource.addProperty("name", "MCP Capabilities");
        capResource.addProperty("description", "Overview of MCP protocol capabilities");
        resources.add(capResource);

        for (Note note : notes.values()) {
            JsonObject noteResource = new JsonObject();
            noteResource.addProperty("uri", "note://" + note.getId());
            noteResource.addProperty("mimeType", "text/plain");
            noteResource.addProperty("name", "Note: " + note.getTitle());
            noteResource.addProperty("description", "Note created on " + note.getCreated());
            resources.add(noteResource);
        }

        result.add("resources", gson.toJsonTree(resources));
        return result;
    }

    public JsonObject readResource(JsonObject params) {
        String uri = params.get("uri").getAsString();
        JsonObject result = new JsonObject();
        List<JsonObject> contents = new ArrayList<>();

        if ("demo://info".equals(uri)) {
            String info = "MCP Demo Server v1.0.0\n\nThis server demonstrates the core capabilities of the Model Context Protocol.";
            JsonObject content = new JsonObject(); content.addProperty("uri", uri); content.addProperty("mimeType","text/plain"); content.addProperty("text", info); contents.add(content);
        } else if ("demo://capabilities".equals(uri)) {
            JsonObject capabilities = new JsonObject(); capabilities.addProperty("protocol", "Model Context Protocol (MCP)"); capabilities.addProperty("version", "1.0.0");
            JsonObject features = new JsonObject(); features.addProperty("tools", "Execute functions with structured input/output"); features.addProperty("resources", "Access and read external data sources"); features.addProperty("prompts", "Use pre-configured prompt templates"); capabilities.add("features", features);
            capabilities.addProperty("transport","stdio"); capabilities.addProperty("documentation","https://modelcontextprotocol.io");
            JsonObject content = new JsonObject(); content.addProperty("uri", uri); content.addProperty("mimeType","application/json"); content.addProperty("text", gson.toJson(capabilities)); contents.add(content);
        } else if (uri.startsWith("note://")) {
            int id = Integer.parseInt(uri.substring(7));
            Note note = notes.get(id);
            if (note == null) throw new IllegalArgumentException("Note not found: " + id);
            String noteText = String.format("Title: %s\nCreated: %s\n\n%s", note.getTitle(), note.getCreated(), note.getContent());
            JsonObject content = new JsonObject(); content.addProperty("uri", uri); content.addProperty("mimeType","text/plain"); content.addProperty("text", noteText); contents.add(content);
        } else {
            throw new IllegalArgumentException("Unknown resource: " + uri);
        }

        result.add("contents", gson.toJsonTree(contents));
        return result;
    }
}
