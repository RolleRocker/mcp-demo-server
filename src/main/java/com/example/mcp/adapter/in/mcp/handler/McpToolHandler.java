package com.example.mcp.adapter.in.mcp.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.mcp.application.port.in.CalculationUseCase;
import com.example.mcp.application.port.in.FileOperationUseCase;
import com.example.mcp.application.port.in.NoteManagementUseCase;
import com.example.mcp.application.port.in.WeatherQueryUseCase;
import com.example.mcp.domain.model.Calculation;
import com.example.mcp.domain.model.FileMetadata;
import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.model.Weather;
import com.example.mcp.domain.valueobject.CityName;
import com.example.mcp.domain.valueobject.FilePath;
import com.example.mcp.domain.valueobject.Operation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * MCP protocol adapter for handling tool-related requests.
 * Translates JSON-RPC tool requests into application use case calls.
 */
public final class McpToolHandler {
    private final Gson gson = new Gson();
    private final CalculationUseCase calculationUseCase;
    private final NoteManagementUseCase noteManagement;
    private final WeatherQueryUseCase weatherQuery;
    private final FileOperationUseCase fileOperation;

    public McpToolHandler(CalculationUseCase calculationUseCase,
                          NoteManagementUseCase noteManagement,
                          WeatherQueryUseCase weatherQuery,
                          FileOperationUseCase fileOperation) {
        this.calculationUseCase = calculationUseCase;
        this.noteManagement = noteManagement;
        this.weatherQuery = weatherQuery;
        this.fileOperation = fileOperation;
    }

    public JsonObject listTools() {
        JsonObject result = new JsonObject();
        List<JsonObject> tools = new ArrayList<>();

        // Calculate tool
        tools.add(createCalculateTool());
        
        // Note tools
        tools.add(createCreateNoteTool());
        tools.add(createListNotesTool());
        
        // Weather tool
        tools.add(createWeatherTool());
        
        // File tools
        tools.add(createReadFileTool());
        tools.add(createWriteFileTool());
        tools.add(createListDirectoryTool());

        result.add("tools", gson.toJsonTree(tools));
        return result;
    }

    public JsonObject callTool(JsonObject params) {
        String name = params.get("name").getAsString();
        JsonObject args = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();
        
        JsonObject result = new JsonObject();
        List<JsonObject> content = new ArrayList<>();

        try {
            String responseText = switch (name) {
                case "calculate" -> handleCalculate(args);
                case "create_note" -> handleCreateNote(args);
                case "list_notes" -> handleListNotes();
                case "get_weather" -> handleGetWeather(args);
                case "read_file" -> handleReadFile(args);
                case "write_file" -> handleWriteFile(args);
                case "list_directory" -> handleListDirectory(args);
                default -> throw new IllegalArgumentException("Unknown tool: " + name);
            };
            
            content.add(createTextContent(responseText));
            result.add("content", gson.toJsonTree(content));
            
        } catch (Exception e) {
            result.addProperty("isError", true);
            content.add(createTextContent("Error: " + e.getMessage()));
            result.add("content", gson.toJsonTree(content));
        }

        return result;
    }

    // Tool handlers
    
    private String handleCalculate(JsonObject args) {
        String operationStr = args.get("operation").getAsString();
        double a = args.get("a").getAsDouble();
        double b = args.get("b").getAsDouble();
        
        Operation operation = Operation.fromString(operationStr);
        Calculation calculation = calculationUseCase.calculate(operation, a, b);
        
        return calculation.format();
    }

    private String handleCreateNote(JsonObject args) {
        String title = args.get("title").getAsString();
        String content = args.get("content").getAsString();
        
        Note note = noteManagement.createNote(title, content);
        
        return String.format("Note created successfully!\nID: %d\nTitle: %s",
            note.getId().getValue(), note.getTitle());
    }

    private String handleListNotes() {
        List<Note> notes = noteManagement.listAllNotes();
        
        if (notes.isEmpty()) {
            return "No notes found. Create one using the create_note tool!";
        }
        
        StringBuilder sb = new StringBuilder("Available notes (").append(notes.size()).append("):\n");
        for (Note note : notes) {
            sb.append("ID ").append(note.getId().getValue())
              .append(": ").append(note.getTitle()).append("\n");
        }
        
        return sb.toString().trim();
    }

    private String handleGetWeather(JsonObject args) {
        String cityStr = args.get("city").getAsString();
        CityName city = new CityName(cityStr);
        
        try {
            Weather weather = weatherQuery.getWeatherForCity(city);
            return weather.format();
        } catch (WeatherQueryUseCase.WeatherServiceException e) {
            return "Error fetching weather: " + e.getMessage();
        }
    }

    private String handleReadFile(JsonObject args) {
        String pathStr = args.get("file_path").getAsString();
        FilePath path = new FilePath(pathStr);
        
        try {
            String content = fileOperation.readFile(path);
            return "File contents of " + pathStr + ":\n\n" + content;
        } catch (FileOperationUseCase.FileOperationException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleWriteFile(JsonObject args) {
        String pathStr = args.get("file_path").getAsString();
        String content = args.get("content").getAsString();
        
        FilePath path = new FilePath(pathStr);
        
        try {
            fileOperation.writeFile(path, content);
            return "File written successfully: " + pathStr;
        } catch (FileOperationUseCase.FileOperationException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleListDirectory(JsonObject args) {
        String pathStr = args.has("directory_path") ? args.get("directory_path").getAsString() : ".";
        FilePath path = new FilePath(pathStr);
        
        try {
            List<FileMetadata> entries = fileOperation.listDirectory(path);
            
            StringBuilder result = new StringBuilder("Contents of ").append(pathStr).append(":\n\n");
            for (FileMetadata entry : entries) {
                result.append(entry.formatListEntry()).append("\n");
            }
            
            return result.toString();
        } catch (FileOperationUseCase.FileOperationException e) {
            return "Error: " + e.getMessage();
        }
    }

    // Tool schema definitions
    
    private JsonObject createCalculateTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "calculate");
        tool.addProperty("description", "Perform basic arithmetic calculations (add, subtract, multiply, divide)");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject props = new JsonObject();
        JsonObject opProp = new JsonObject();
        opProp.addProperty("type", "string");
        opProp.addProperty("description", "The arithmetic operation to perform");
        opProp.add("enum", gson.toJsonTree(Arrays.asList("add", "subtract", "multiply", "divide")));
        props.add("operation", opProp);
        
        JsonObject aProp = new JsonObject();
        aProp.addProperty("type", "number");
        aProp.addProperty("description", "First number");
        props.add("a", aProp);
        
        JsonObject bProp = new JsonObject();
        bProp.addProperty("type", "number");
        bProp.addProperty("description", "Second number");
        props.add("b", bProp);
        
        schema.add("properties", props);
        schema.add("required", gson.toJsonTree(Arrays.asList("operation", "a", "b")));
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createCreateNoteTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "create_note");
        tool.addProperty("description", "Create a new note with a title and content");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject props = new JsonObject();
        JsonObject titleProp = new JsonObject();
        titleProp.addProperty("type", "string");
        titleProp.addProperty("description", "The title of the note");
        props.add("title", titleProp);
        
        JsonObject contentProp = new JsonObject();
        contentProp.addProperty("type", "string");
        contentProp.addProperty("description", "The content of the note");
        props.add("content", contentProp);
        
        schema.add("properties", props);
        schema.add("required", gson.toJsonTree(Arrays.asList("title", "content")));
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createListNotesTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "list_notes");
        tool.addProperty("description", "List all notes with their IDs and titles");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", new JsonObject());
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createWeatherTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "get_weather");
        tool.addProperty("description", "Get real weather information for a city");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject props = new JsonObject();
        JsonObject cityProp = new JsonObject();
        cityProp.addProperty("type", "string");
        cityProp.addProperty("description", "The city name");
        props.add("city", cityProp);
        
        schema.add("properties", props);
        schema.add("required", gson.toJsonTree(Arrays.asList("city")));
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createReadFileTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "read_file");
        tool.addProperty("description", "Read the contents of a text file");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject props = new JsonObject();
        JsonObject pathProp = new JsonObject();
        pathProp.addProperty("type", "string");
        pathProp.addProperty("description", "The path to the file to read");
        props.add("file_path", pathProp);
        
        schema.add("properties", props);
        schema.add("required", gson.toJsonTree(Arrays.asList("file_path")));
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createWriteFileTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "write_file");
        tool.addProperty("description", "Write content to a text file (creates or overwrites)");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject props = new JsonObject();
        JsonObject pathProp = new JsonObject();
        pathProp.addProperty("type", "string");
        pathProp.addProperty("description", "The path to the file to write");
        props.add("file_path", pathProp);
        
        JsonObject contentProp = new JsonObject();
        contentProp.addProperty("type", "string");
        contentProp.addProperty("description", "The content to write to the file");
        props.add("content", contentProp);
        
        schema.add("properties", props);
        schema.add("required", gson.toJsonTree(Arrays.asList("file_path", "content")));
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createListDirectoryTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "list_directory");
        tool.addProperty("description", "List files and directories in a folder");
        
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject props = new JsonObject();
        JsonObject pathProp = new JsonObject();
        pathProp.addProperty("type", "string");
        pathProp.addProperty("description", "The directory path to list (defaults to current directory)");
        props.add("directory_path", pathProp);
        
        schema.add("properties", props);
        tool.add("inputSchema", schema);
        
        return tool;
    }

    private JsonObject createTextContent(String text) {
        JsonObject content = new JsonObject();
        content.addProperty("type", "text");
        content.addProperty("text", text);
        return content;
    }
}
