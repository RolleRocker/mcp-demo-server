package com.example.mcp.adapter.in.mcp.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mcp.application.port.in.PromptGenerationUseCase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * MCP protocol adapter for handling prompt-related requests.
 * Translates JSON-RPC prompt requests into application use case calls.
 */
public final class McpPromptHandler {
    private final Gson gson = new Gson();
    private final PromptGenerationUseCase promptGeneration;

    public McpPromptHandler(PromptGenerationUseCase promptGeneration) {
        this.promptGeneration = promptGeneration;
    }

    public JsonObject listPrompts() {
        List<PromptGenerationUseCase.Prompt> prompts = promptGeneration.listPrompts();
        
        JsonObject result = new JsonObject();
        List<JsonObject> promptsJson = new ArrayList<>();
        
        for (PromptGenerationUseCase.Prompt prompt : prompts) {
            JsonObject promptJson = new JsonObject();
            promptJson.addProperty("name", prompt.name());
            promptJson.addProperty("description", prompt.description());
            
            List<JsonObject> argsJson = new ArrayList<>();
            for (PromptGenerationUseCase.PromptArgument arg : prompt.arguments()) {
                JsonObject argJson = new JsonObject();
                argJson.addProperty("name", arg.name());
                argJson.addProperty("description", arg.description());
                argJson.addProperty("required", arg.required());
                argsJson.add(argJson);
            }
            
            promptJson.add("arguments", gson.toJsonTree(argsJson));
            promptsJson.add(promptJson);
        }
        
        result.add("prompts", gson.toJsonTree(promptsJson));
        return result;
    }

    public JsonObject getPrompt(JsonObject params) {
        String name = params.get("name").getAsString();
        
        // Extract arguments from params
        Map<String, String> arguments = new HashMap<>();
        if (params.has("arguments")) {
            JsonObject argsJson = params.getAsJsonObject("arguments");
            for (String key : argsJson.keySet()) {
                arguments.put(key, argsJson.get(key).getAsString());
            }
        }
        
        try {
            List<PromptGenerationUseCase.PromptMessage> messages = 
                promptGeneration.generatePrompt(name, arguments);
            
            JsonObject result = new JsonObject();
            List<JsonObject> messagesJson = new ArrayList<>();
            
            for (PromptGenerationUseCase.PromptMessage message : messages) {
                JsonObject messageJson = new JsonObject();
                messageJson.addProperty("role", message.role());
                
                JsonObject content = new JsonObject();
                content.addProperty("type", "text");
                content.addProperty("text", message.text());
                messageJson.add("content", content);
                
                messagesJson.add(messageJson);
            }
            
            result.add("messages", gson.toJsonTree(messagesJson));
            return result;
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to generate prompt: " + name, e);
        }
    }
}
