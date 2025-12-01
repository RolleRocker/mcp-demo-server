package com.example.mcp.adapter.in.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.example.mcp.adapter.in.mcp.handler.McpPromptHandler;
import com.example.mcp.adapter.in.mcp.handler.McpResourceHandler;
import com.example.mcp.adapter.in.mcp.handler.McpToolHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * MCP protocol server implementing JSON-RPC over stdio.
 * This is the driving adapter that translates MCP protocol into application use cases.
 */
public final class McpServer {
    private final Gson gson = new GsonBuilder().create();
    private final McpToolHandler toolHandler;
    private final McpResourceHandler resourceHandler;
    private final McpPromptHandler promptHandler;

    public McpServer(McpToolHandler toolHandler,
                     McpResourceHandler resourceHandler,
                     McpPromptHandler promptHandler) {
        this.toolHandler = toolHandler;
        this.resourceHandler = resourceHandler;
        this.promptHandler = promptHandler;
    }

    /**
     * Runs the MCP server, reading JSON-RPC requests from stdin and writing responses to stdout.
     * Logs go to stderr to avoid interfering with the protocol.
     */
    public void run() {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(System.out, true);

        System.err.println("[MCP] Starting MCP demo server (hexagonal architecture)");

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonObject request = gson.fromJson(line, JsonObject.class);
                    JsonObject response = handleRequest(request);
                    writer.println(gson.toJson(response));
                } catch (JsonSyntaxException e) {
                    System.err.println("[MCP] Invalid JSON: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("[MCP] Request processing failed: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        } catch (IOException e) {
            System.err.println("[MCP] IO error: " + e.getMessage());
            System.exit(1);
        }
    }

    private JsonObject handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : "";
        JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();

        System.err.println("[MCP] Request: " + method);

        // Handle notifications (no response needed)
        if (!request.has("id")) {
            if ("notifications/initialized".equals(method)) {
                System.err.println("[MCP] Server initialization complete");
            }
            return new JsonObject(); // Empty response for notifications
        }

        // Handle requests (response required)
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", request.get("id"));

        try {
            JsonObject result = switch (method) {
                case "initialize" -> handleInitialize();
                case "tools/list" -> toolHandler.listTools();
                case "tools/call" -> toolHandler.callTool(params);
                case "resources/list" -> resourceHandler.listResources();
                case "resources/read" -> resourceHandler.readResource(params);
                case "prompts/list" -> promptHandler.listPrompts();
                case "prompts/get" -> promptHandler.getPrompt(params);
                default -> throw new IllegalArgumentException("Method not found: " + method);
            };
            
            response.add("result", result);
            
        } catch (IllegalArgumentException e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32601);
            error.addProperty("message", e.getMessage());
            response.add("error", error);
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32603);
            error.addProperty("message", "Internal error: " + e.getMessage());
            response.add("error", error);
            e.printStackTrace(System.err);
        }

        return response;
    }

    private JsonObject handleInitialize() {
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", "2024-11-05");

        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "mcp-demo-server");
        serverInfo.addProperty("version", "1.0.0");
        result.add("serverInfo", serverInfo);

        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", new JsonObject());
        capabilities.add("resources", new JsonObject());
        capabilities.add("prompts", new JsonObject());
        result.add("capabilities", capabilities);

        System.err.println("[MCP] Initialized with protocol version 2024-11-05");
        return result;
    }
}
