package com.example.mcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Demo Server - Demonstrates the core capabilities of the Model Context Protocol
 * 
 * This server implements:
 * - Tools: Interactive functions (calculate, create_note, list_notes, get_weather)
 * - Resources: Exposed data sources (server info, capabilities, notes)
 * - Prompts: Pre-configured prompt templates
 */
public class DemoServer {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Integer, Note> notes = new ConcurrentHashMap<>();
    private static int noteIdCounter = 1;
    
    private final BufferedReader reader;
    private final PrintWriter writer;

    public DemoServer() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.writer = new PrintWriter(System.out, true);
    }

    public static void main(String[] args) {
        System.err.println("MCP Demo Server starting on stdio...");
        DemoServer server = new DemoServer();
        server.run();
    }

    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                handleRequest(line);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(1);
        }
    }

    private void handleRequest(String jsonRequest) {
        try {
            JsonObject request = gson.fromJson(jsonRequest, JsonObject.class);
            String method = request.get("method").getAsString();
            JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();
            
            JsonObject response = new JsonObject();
            response.addProperty("jsonrpc", "2.0");
            
            if (request.has("id")) {
                response.add("id", request.get("id"));
            }

            switch (method) {
                case "initialize":
                    response.add("result", handleInitialize());
                    break;
                case "tools/list":
                    response.add("result", handleListTools());
                    break;
                case "tools/call":
                    response.add("result", handleCallTool(params));
                    break;
                case "resources/list":
                    response.add("result", handleListResources());
                    break;
                case "resources/read":
                    response.add("result", handleReadResource(params));
                    break;
                case "prompts/list":
                    response.add("result", handleListPrompts());
                    break;
                case "prompts/get":
                    response.add("result", handleGetPrompt(params));
                    break;
                default:
                    JsonObject error = new JsonObject();
                    error.addProperty("code", -32601);
                    error.addProperty("message", "Method not found: " + method);
                    response.add("error", error);
            }

            writer.println(gson.toJson(response));
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace(System.err);
        }
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
        
        return result;
    }

    // ============================================================================
    // TOOLS - Demonstrate tool calling capabilities
    // ============================================================================

    private JsonObject handleListTools() {
        JsonObject result = new JsonObject();
        List<JsonObject> tools = new ArrayList<>();
        
        // Calculate tool
        JsonObject calculateTool = new JsonObject();
        calculateTool.addProperty("name", "calculate");
        calculateTool.addProperty("description", "Perform basic arithmetic calculations (add, subtract, multiply, divide)");
        
        JsonObject calcSchema = new JsonObject();
        calcSchema.addProperty("type", "object");
        JsonObject calcProps = new JsonObject();
        
        JsonObject opProp = new JsonObject();
        opProp.addProperty("type", "string");
        opProp.addProperty("description", "The arithmetic operation to perform");
        opProp.add("enum", gson.toJsonTree(Arrays.asList("add", "subtract", "multiply", "divide")));
        calcProps.add("operation", opProp);
        
        JsonObject aProp = new JsonObject();
        aProp.addProperty("type", "number");
        aProp.addProperty("description", "First number");
        calcProps.add("a", aProp);
        
        JsonObject bProp = new JsonObject();
        bProp.addProperty("type", "number");
        bProp.addProperty("description", "Second number");
        calcProps.add("b", bProp);
        
        calcSchema.add("properties", calcProps);
        calcSchema.add("required", gson.toJsonTree(Arrays.asList("operation", "a", "b")));
        calculateTool.add("inputSchema", calcSchema);
        tools.add(calculateTool);
        
        // Create note tool
        JsonObject createNoteTool = new JsonObject();
        createNoteTool.addProperty("name", "create_note");
        createNoteTool.addProperty("description", "Create a new note with a title and content");
        
        JsonObject noteSchema = new JsonObject();
        noteSchema.addProperty("type", "object");
        JsonObject noteProps = new JsonObject();
        
        JsonObject titleProp = new JsonObject();
        titleProp.addProperty("type", "string");
        titleProp.addProperty("description", "The title of the note");
        noteProps.add("title", titleProp);
        
        JsonObject contentProp = new JsonObject();
        contentProp.addProperty("type", "string");
        contentProp.addProperty("description", "The content of the note");
        noteProps.add("content", contentProp);
        
        noteSchema.add("properties", noteProps);
        noteSchema.add("required", gson.toJsonTree(Arrays.asList("title", "content")));
        createNoteTool.add("inputSchema", noteSchema);
        tools.add(createNoteTool);
        
        // List notes tool
        JsonObject listNotesTool = new JsonObject();
        listNotesTool.addProperty("name", "list_notes");
        listNotesTool.addProperty("description", "List all notes with their IDs and titles");
        
        JsonObject listSchema = new JsonObject();
        listSchema.addProperty("type", "object");
        listSchema.add("properties", new JsonObject());
        listNotesTool.add("inputSchema", listSchema);
        tools.add(listNotesTool);
        
        // Get weather tool
        JsonObject weatherTool = new JsonObject();
        weatherTool.addProperty("name", "get_weather");
        weatherTool.addProperty("description", "Get simulated weather information for a city");
        
        JsonObject weatherSchema = new JsonObject();
        weatherSchema.addProperty("type", "object");
        JsonObject weatherProps = new JsonObject();
        
        JsonObject cityProp = new JsonObject();
        cityProp.addProperty("type", "string");
        cityProp.addProperty("description", "The city name");
        weatherProps.add("city", cityProp);
        
        weatherSchema.add("properties", weatherProps);
        weatherSchema.add("required", gson.toJsonTree(Arrays.asList("city")));
        weatherTool.add("inputSchema", weatherSchema);
        tools.add(weatherTool);
        
        result.add("tools", gson.toJsonTree(tools));
        return result;
    }

    private JsonObject handleCallTool(JsonObject params) {
        String name = params.get("name").getAsString();
        JsonObject args = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();
        
        JsonObject result = new JsonObject();
        List<JsonObject> content = new ArrayList<>();
        
        try {
            switch (name) {
                case "calculate":
                    content.add(createTextContent(handleCalculate(args)));
                    break;
                case "create_note":
                    content.add(createTextContent(handleCreateNote(args)));
                    break;
                case "list_notes":
                    content.add(createTextContent(handleListNotes()));
                    break;
                case "get_weather":
                    content.add(createTextContent(handleGetWeather(args)));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tool: " + name);
            }
            
            result.add("content", gson.toJsonTree(content));
        } catch (Exception e) {
            result.addProperty("isError", true);
            JsonObject errorContent = new JsonObject();
            errorContent.addProperty("type", "text");
            errorContent.addProperty("text", "Error: " + e.getMessage());
            content.add(errorContent);
            result.add("content", gson.toJsonTree(content));
        }
        
        return result;
    }

    private String handleCalculate(JsonObject args) {
        String operation = args.get("operation").getAsString();
        double a = args.get("a").getAsDouble();
        double b = args.get("b").getAsDouble();
        double result;
        String symbol;
        
        switch (operation) {
            case "add":
                result = a + b;
                symbol = "+";
                break;
            case "subtract":
                result = a - b;
                symbol = "-";
                break;
            case "multiply":
                result = a * b;
                symbol = "×";
                break;
            case "divide":
                if (b == 0) {
                    throw new ArithmeticException("Division by zero is not allowed");
                }
                result = a / b;
                symbol = "÷";
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        
        return String.format("Result: %.2f %s %.2f = %.2f", a, symbol, b, result);
    }

    private String handleCreateNote(JsonObject args) {
        String title = args.get("title").getAsString();
        String content = args.get("content").getAsString();
        
        int id = noteIdCounter++;
        Note note = new Note(id, title, content, LocalDateTime.now());
        notes.put(id, note);
        
        return String.format("Note created successfully!\nID: %d\nTitle: %s", id, title);
    }

    private String handleListNotes() {
        if (notes.isEmpty()) {
            return "No notes found. Create one using the create_note tool!";
        }
        
        StringBuilder sb = new StringBuilder("Available notes (").append(notes.size()).append("):\n");
        notes.values().stream()
            .sorted(Comparator.comparingInt(Note::getId))
            .forEach(note -> sb.append("ID ").append(note.getId()).append(": ").append(note.getTitle()).append("\n"));
        
        return sb.toString().trim();
    }

    private String handleGetWeather(JsonObject args) {
        String city = args.get("city").getAsString();
        
        Random random = new Random();
        String[] conditions = {"Sunny", "Cloudy", "Rainy", "Partly Cloudy"};
        String condition = conditions[random.nextInt(conditions.length)];
        int temp = random.nextInt(30) + 10;
        int wind = random.nextInt(20);
        
        return String.format("Weather in %s:\n🌡️ Temperature: %d°C\n☁️ Condition: %s\n💨 Wind: %d km/h", 
            city, temp, condition, wind);
    }

    // ============================================================================
    // RESOURCES - Demonstrate resource exposure
    // ============================================================================

    private JsonObject handleListResources() {
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

    private JsonObject handleReadResource(JsonObject params) {
        String uri = params.get("uri").getAsString();
        
        JsonObject result = new JsonObject();
        List<JsonObject> contents = new ArrayList<>();
        
        if ("demo://info".equals(uri)) {
            String info = "MCP Demo Server v1.0.0\n\n" +
                "This server demonstrates the core capabilities of the Model Context Protocol:\n\n" +
                "🛠️  TOOLS: Interactive functions that can be called\n" +
                "   - calculate: Perform arithmetic operations\n" +
                "   - create_note: Create and store notes\n" +
                "   - list_notes: View all saved notes\n" +
                "   - get_weather: Get simulated weather data\n\n" +
                "📄 RESOURCES: Exposed data that can be read\n" +
                "   - Server information (this document)\n" +
                "   - Capabilities overview\n" +
                "   - Dynamic note resources\n\n" +
                "💬 PROMPTS: Pre-configured prompt templates\n" +
                "   - Helpful assistant persona\n" +
                "   - Code review assistant\n" +
                "   - Note summarizer\n\n" +
                "The MCP allows AI models to interact with external tools and data sources in a standardized way.";
            
            JsonObject content = new JsonObject();
            content.addProperty("uri", uri);
            content.addProperty("mimeType", "text/plain");
            content.addProperty("text", info);
            contents.add(content);
        } else if ("demo://capabilities".equals(uri)) {
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
            
            JsonObject content = new JsonObject();
            content.addProperty("uri", uri);
            content.addProperty("mimeType", "application/json");
            content.addProperty("text", gson.toJson(capabilities));
            contents.add(content);
        } else if (uri.startsWith("note://")) {
            int id = Integer.parseInt(uri.substring(7));
            Note note = notes.get(id);
            
            if (note == null) {
                throw new IllegalArgumentException("Note not found: " + id);
            }
            
            String noteText = String.format("Title: %s\nCreated: %s\n\n%s", 
                note.getTitle(), note.getCreated(), note.getContent());
            
            JsonObject content = new JsonObject();
            content.addProperty("uri", uri);
            content.addProperty("mimeType", "text/plain");
            content.addProperty("text", noteText);
            contents.add(content);
        } else {
            throw new IllegalArgumentException("Unknown resource: " + uri);
        }
        
        result.add("contents", gson.toJsonTree(contents));
        return result;
    }

    // ============================================================================
    // PROMPTS - Demonstrate prompt templates
    // ============================================================================

    private JsonObject handleListPrompts() {
        JsonObject result = new JsonObject();
        List<JsonObject> prompts = new ArrayList<>();
        
        JsonObject helpfulPrompt = new JsonObject();
        helpfulPrompt.addProperty("name", "helpful_assistant");
        helpfulPrompt.addProperty("description", "A helpful and friendly assistant persona");
        List<JsonObject> helpfulArgs = new ArrayList<>();
        JsonObject taskArg = new JsonObject();
        taskArg.addProperty("name", "task");
        taskArg.addProperty("description", "The task to help with");
        taskArg.addProperty("required", true);
        helpfulArgs.add(taskArg);
        helpfulPrompt.add("arguments", gson.toJsonTree(helpfulArgs));
        prompts.add(helpfulPrompt);
        
        JsonObject reviewPrompt = new JsonObject();
        reviewPrompt.addProperty("name", "code_reviewer");
        reviewPrompt.addProperty("description", "Review code and provide constructive feedback");
        List<JsonObject> reviewArgs = new ArrayList<>();
        JsonObject langArg = new JsonObject();
        langArg.addProperty("name", "language");
        langArg.addProperty("description", "Programming language");
        langArg.addProperty("required", true);
        reviewArgs.add(langArg);
        JsonObject codeArg = new JsonObject();
        codeArg.addProperty("name", "code");
        codeArg.addProperty("description", "Code to review");
        codeArg.addProperty("required", true);
        reviewArgs.add(codeArg);
        reviewPrompt.add("arguments", gson.toJsonTree(reviewArgs));
        prompts.add(reviewPrompt);
        
        JsonObject summarizePrompt = new JsonObject();
        summarizePrompt.addProperty("name", "summarize_notes");
        summarizePrompt.addProperty("description", "Summarize all notes in the system");
        summarizePrompt.add("arguments", gson.toJsonTree(new ArrayList<>()));
        prompts.add(summarizePrompt);
        
        result.add("prompts", gson.toJsonTree(prompts));
        return result;
    }

    private JsonObject handleGetPrompt(JsonObject params) {
        String name = params.get("name").getAsString();
        JsonObject args = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();
        
        JsonObject result = new JsonObject();
        List<JsonObject> messages = new ArrayList<>();
        
        switch (name) {
            case "helpful_assistant":
                String task = args.has("task") ? args.get("task").getAsString() : "general assistance";
                String helpfulText = String.format(
                    "You are a helpful, friendly, and knowledgeable assistant. Please help me with the following task:\n\n%s\n\nProvide clear, accurate, and actionable guidance.",
                    task);
                messages.add(createMessage("user", helpfulText));
                break;
                
            case "code_reviewer":
                String language = args.has("language") ? args.get("language").getAsString() : "unknown";
                String code = args.has("code") ? args.get("code").getAsString() : "";
                String reviewText = String.format(
                    "Please review the following %s code and provide constructive feedback:\n\n```%s\n%s\n```\n\nConsider:\n- Code quality and readability\n- Potential bugs or issues\n- Performance concerns\n- Best practices\n- Suggestions for improvement",
                    language, language, code);
                messages.add(createMessage("user", reviewText));
                break;
                
            case "summarize_notes":
                if (notes.isEmpty()) {
                    messages.add(createMessage("user", 
                        "There are no notes to summarize. Please create some notes first using the create_note tool."));
                } else {
                    StringBuilder notesText = new StringBuilder("Please provide a concise summary of the following notes:\n\n");
                    notes.values().stream()
                        .sorted(Comparator.comparingInt(Note::getId))
                        .forEach(note -> {
                            notesText.append("**").append(note.getTitle()).append("** (ID: ").append(note.getId()).append(")\n");
                            notesText.append(note.getContent()).append("\n\n---\n\n");
                        });
                    messages.add(createMessage("user", notesText.toString()));
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown prompt: " + name);
        }
        
        result.add("messages", gson.toJsonTree(messages));
        return result;
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private JsonObject createTextContent(String text) {
        JsonObject content = new JsonObject();
        content.addProperty("type", "text");
        content.addProperty("text", text);
        return content;
    }

    private JsonObject createMessage(String role, String text) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        
        JsonObject content = new JsonObject();
        content.addProperty("type", "text");
        content.addProperty("text", text);
        message.add("content", content);
        
        return message;
    }

    // ============================================================================
    // Note Class
    // ============================================================================

    static class Note {
        private final int id;
        private final String title;
        private final String content;
        private final LocalDateTime created;

        public Note(int id, String title, String content, LocalDateTime created) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.created = created;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDateTime getCreated() { return created; }
    }
}
