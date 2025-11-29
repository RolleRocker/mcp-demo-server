package com.example.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * New entrypoint replacing DemoServer.java. Keeps behavior but lives in `Main`.
 */
public class Main {
    private static final Gson gson = new GsonBuilder().create();

    public static void main(String[] args) {
        Map<Integer, Note> notes = new ConcurrentHashMap<>();
        AtomicInteger noteIdCounter = new AtomicInteger(1);

        ToolsManager toolsManager = new ToolsManager(notes, noteIdCounter);
        ResourceManager resourceManager = new ResourceManager(notes);
        PromptManager promptManager = new PromptManager(notes);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(System.out, true);

        SimpleLogger.log("Starting MCP demo server (Main entrypoint)");

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonObject request = gson.fromJson(line, JsonObject.class);
                    String method = request.has("method") ? request.get("method").getAsString() : "";
                    JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();

                    SimpleLogger.log("[REQUEST] Method: " + method);

                    if (!request.has("id")) {
                        // notifications
                        if ("notifications/initialized".equals(method)) {
                            SimpleLogger.log("[NOTIFICATION] Server initialization complete");
                        }
                        continue;
                    }

                    JsonObject response = new JsonObject();
                    response.addProperty("jsonrpc", "2.0");
                    response.add("id", request.get("id"));

                    switch (method) {
                        case "initialize" -> response.add("result", initializeResult());
                        case "tools/list" -> response.add("result", toolsManager.listTools());
                        case "tools/call" -> response.add("result", toolsManager.callTool(params));
                        case "resources/list" -> response.add("result", resourceManager.listResources());
                        case "resources/read" -> response.add("result", resourceManager.readResource(params));
                        case "prompts/list" -> response.add("result", promptManager.listPrompts());
                        case "prompts/get" -> response.add("result", promptManager.getPrompt(params));
                        default -> {
                            JsonObject error = new JsonObject();
                            error.addProperty("code", -32601);
                            error.addProperty("message", "Method not found: " + method);
                            response.add("error", error);
                        }
                    }

                    writer.println(gson.toJson(response));
                } catch (com.google.gson.JsonSyntaxException e) {
                    SimpleLogger.log("[ERROR] Invalid JSON: " + e.getMessage());
                    e.printStackTrace(System.err);
                } catch (IllegalArgumentException e) {
                    SimpleLogger.log("[ERROR] Request processing failed: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        } catch (IOException e) {
            SimpleLogger.log("IO error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static JsonObject initializeResult() {
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

        return result;
    }
}
