package com.example.mcp;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ToolsManager {
    private final Gson gson = new Gson();
    private final Map<Integer, Note> notes;
    private final AtomicInteger noteIdCounter;

    public ToolsManager(Map<Integer, Note> notes, AtomicInteger counter) {
        this.notes = notes;
        this.noteIdCounter = counter;
    }

    public JsonObject listTools() {
        JsonObject result = new JsonObject();
        List<JsonObject> tools = new ArrayList<>();

        // (same tool definitions as before, minimized here)
        // Calculate
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
        JsonObject aProp = new JsonObject(); aProp.addProperty("type", "number"); aProp.addProperty("description", "First number"); calcProps.add("a", aProp);
        JsonObject bProp = new JsonObject(); bProp.addProperty("type", "number"); bProp.addProperty("description", "Second number"); calcProps.add("b", bProp);
        calcSchema.add("properties", calcProps);
        calcSchema.add("required", gson.toJsonTree(Arrays.asList("operation", "a", "b")));
        calculateTool.add("inputSchema", calcSchema);
        tools.add(calculateTool);

        // create_note, list_notes, get_weather (omitted here for brevity but included in full implementation below)
        // Create note
        JsonObject createNoteTool = new JsonObject();
        createNoteTool.addProperty("name", "create_note");
        createNoteTool.addProperty("description", "Create a new note with a title and content");
        JsonObject noteSchema = new JsonObject(); noteSchema.addProperty("type","object");
        JsonObject noteProps = new JsonObject();
        JsonObject titleProp = new JsonObject(); titleProp.addProperty("type","string"); titleProp.addProperty("description","The title of the note"); noteProps.add("title", titleProp);
        JsonObject contentProp = new JsonObject(); contentProp.addProperty("type","string"); contentProp.addProperty("description","The content of the note"); noteProps.add("content", contentProp);
        noteSchema.add("properties", noteProps); noteSchema.add("required", gson.toJsonTree(Arrays.asList("title","content")));
        createNoteTool.add("inputSchema", noteSchema); tools.add(createNoteTool);

        // list_notes
        JsonObject listNotesTool = new JsonObject(); listNotesTool.addProperty("name","list_notes"); listNotesTool.addProperty("description","List all notes with their IDs and titles");
        JsonObject listSchema = new JsonObject(); listSchema.addProperty("type","object"); listSchema.add("properties", new JsonObject()); listNotesTool.add("inputSchema", listSchema); tools.add(listNotesTool);

        // get_weather
        JsonObject weatherTool = new JsonObject(); weatherTool.addProperty("name","get_weather"); weatherTool.addProperty("description","Get real weather information for a city");
        JsonObject weatherSchema = new JsonObject(); weatherSchema.addProperty("type","object"); JsonObject weatherProps = new JsonObject(); JsonObject cityProp = new JsonObject(); cityProp.addProperty("type","string"); cityProp.addProperty("description","The city name"); weatherProps.add("city", cityProp); weatherSchema.add("properties", weatherProps); weatherSchema.add("required", gson.toJsonTree(Arrays.asList("city")));
        weatherTool.add("inputSchema", weatherSchema); tools.add(weatherTool);

        // file tools: read_file, write_file, list_directory
        JsonObject readFileTool = new JsonObject(); readFileTool.addProperty("name","read_file"); readFileTool.addProperty("description","Read the contents of a text file");
        JsonObject readFileSchema = new JsonObject(); readFileSchema.addProperty("type","object"); JsonObject readFileProps = new JsonObject(); JsonObject filePathProp = new JsonObject(); filePathProp.addProperty("type","string"); filePathProp.addProperty("description","The path to the file to read"); readFileProps.add("file_path", filePathProp); readFileSchema.add("properties", readFileProps); readFileSchema.add("required", gson.toJsonTree(Arrays.asList("file_path"))); readFileTool.add("inputSchema", readFileSchema); tools.add(readFileTool);

        JsonObject writeFileTool = new JsonObject(); writeFileTool.addProperty("name","write_file"); writeFileTool.addProperty("description","Write content to a text file (creates or overwrites)");
        JsonObject writeFileSchema = new JsonObject(); writeFileSchema.addProperty("type","object"); JsonObject writeFileProps = new JsonObject(); JsonObject writeFilePathProp = new JsonObject(); writeFilePathProp.addProperty("type","string"); writeFilePathProp.addProperty("description","The path to the file to write"); writeFileProps.add("file_path", writeFilePathProp); JsonObject writeContentProp = new JsonObject(); writeContentProp.addProperty("type","string"); writeContentProp.addProperty("description","The content to write to the file"); writeFileProps.add("content", writeContentProp); writeFileSchema.add("properties", writeFileProps); writeFileSchema.add("required", gson.toJsonTree(Arrays.asList("file_path","content"))); writeFileTool.add("inputSchema", writeFileSchema); tools.add(writeFileTool);

        JsonObject listDirTool = new JsonObject(); listDirTool.addProperty("name","list_directory"); listDirTool.addProperty("description","List files and directories in a folder"); JsonObject listDirSchema = new JsonObject(); listDirSchema.addProperty("type","object"); JsonObject listDirProps = new JsonObject(); JsonObject dirPathProp = new JsonObject(); dirPathProp.addProperty("type","string"); dirPathProp.addProperty("description","The directory path to list (defaults to current directory)"); listDirProps.add("directory_path", dirPathProp); listDirSchema.add("properties", listDirProps); listDirTool.add("inputSchema", listDirSchema); tools.add(listDirTool);

        result.add("tools", gson.toJsonTree(tools));
        return result;
    }

    public JsonObject callTool(JsonObject params) {
        String name = params.get("name").getAsString();
        JsonObject args = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();
        JsonObject result = new JsonObject();
        List<JsonObject> content = new ArrayList<>();

        try {
            switch (name) {
                case "calculate": content.add(createTextContent(handleCalculate(args))); break;
                case "create_note": content.add(createTextContent(handleCreateNote(args))); break;
                case "list_notes": content.add(createTextContent(handleListNotes())); break;
                case "get_weather": content.add(createTextContent(handleGetWeather(args))); break;
                case "read_file": content.add(createTextContent(handleReadFile(args))); break;
                case "write_file": content.add(createTextContent(handleWriteFile(args))); break;
                case "list_directory": content.add(createTextContent(handleListDirectory(args))); break;
                default: throw new IllegalArgumentException("Unknown tool: " + name);
            }
            result.add("content", gson.toJsonTree(content));
        } catch (Exception e) {
            result.addProperty("isError", true);
            JsonObject errorContent = new JsonObject(); errorContent.addProperty("type", "text"); errorContent.addProperty("text", "Error: " + e.getMessage()); content.add(errorContent); result.add("content", gson.toJsonTree(content));
        }

        return result;
    }

    // --- tool implementations ---
    private String handleCalculate(JsonObject args) {
        String operation = args.get("operation").getAsString();
        double a = args.get("a").getAsDouble();
        double b = args.get("b").getAsDouble();
        double result; String symbol;
        switch (operation) {
            case "add": result = a + b; symbol = "+"; break;
            case "subtract": result = a - b; symbol = "-"; break;
            case "multiply": result = a * b; symbol = "×"; break;
            case "divide": if (b == 0) throw new ArithmeticException("Division by zero is not allowed"); result = a / b; symbol = "÷"; break;
            default: throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        return String.format("Result: %.2f %s %.2f = %.2f", a, symbol, b, result);
    }

    private String handleCreateNote(JsonObject args) {
        String title = args.get("title").getAsString();
        String content = args.get("content").getAsString();
        int id = noteIdCounter.getAndIncrement();
        Note note = new Note(id, title, content, java.time.LocalDateTime.now());
        notes.put(id, note);
        SimpleLogger.log("[NOTE] Created note ID=" + id + " title='" + title + "'");
        return String.format("Note created successfully!\nID: %d\nTitle: %s", id, title);
    }

    private String handleListNotes() {
        if (notes.isEmpty()) return "No notes found. Create one using the create_note tool!";
        StringBuilder sb = new StringBuilder("Available notes (" ).append(notes.size()).append("):\n");
        notes.values().stream().sorted(Comparator.comparingInt(Note::getId)).forEach(note -> sb.append("ID ").append(note.getId()).append(": ").append(note.getTitle()).append("\n"));
        return sb.toString().trim();
    }

    private String handleGetWeather(JsonObject args) {
        String city = args.get("city").getAsString();
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // Step 1: Get coordinates for the city using geocoding API
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity + "&count=1&language=en&format=json";
            
            HttpRequest geoRequest = HttpRequest.newBuilder().uri(URI.create(geoUrl)).GET().timeout(java.time.Duration.ofSeconds(10)).build();
            HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());
            
            if (geoResponse.statusCode() != 200) {
                return "Error: Could not reach geocoding service (HTTP " + geoResponse.statusCode() + ")";
            }
            
            String geoBody = geoResponse.body();
            JsonObject geoData;
            try {
                geoData = gson.fromJson(geoBody, JsonObject.class);
            } catch (Exception e) {
                SimpleLogger.log("[WEATHER] Failed to parse geocoding response: " + e.getMessage());
                return "Error: Invalid response from geocoding service";
            }
            
            JsonElement resultsElem = geoData.get("results");
            
            if (resultsElem == null || !resultsElem.isJsonArray() || resultsElem.getAsJsonArray().size() == 0) {
                return "Error: City '" + city + "' not found in database";
            }
            
            JsonObject cityInfo = resultsElem.getAsJsonArray().get(0).getAsJsonObject();
            double latitude = cityInfo.get("latitude").getAsDouble();
            double longitude = cityInfo.get("longitude").getAsDouble();
            String countryName = cityInfo.has("country") ? cityInfo.get("country").getAsString() : "Unknown";
            
            // Step 2: Get weather for the coordinates
            String weatherUrl = String.format(Locale.US, "https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f&current=temperature_2m,weather_code,wind_speed_10m&temperature_unit=celsius", 
                latitude, longitude);
            
            HttpRequest weatherRequest = HttpRequest.newBuilder().uri(URI.create(weatherUrl)).GET().timeout(java.time.Duration.ofSeconds(10)).build();
            HttpResponse<String> weatherResponse = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());
            
            if (weatherResponse.statusCode() != 200) {
                return "Error: Could not reach weather service (HTTP " + weatherResponse.statusCode() + ")";
            }
            
            String weatherBody = weatherResponse.body();
            JsonObject weatherData = gson.fromJson(weatherBody, JsonObject.class);
            JsonObject current = weatherData.getAsJsonObject("current");
            
            double temp = current.get("temperature_2m").getAsDouble();
            int weatherCode = current.get("weather_code").getAsInt();
            double windSpeed = current.get("wind_speed_10m").getAsDouble();
            
            String condition = getWeatherCondition(weatherCode);
            SimpleLogger.log("[WEATHER] Fetched real weather for " + city + " (" + countryName + ")");
            
            return String.format("Weather in %s (%s):\n🌡️ Temperature: %.1f°C\n☁️ Condition: %s\n💨 Wind: %.1f km/h", 
                city, countryName, temp, condition, windSpeed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Request interrupted while fetching weather";
        } catch (Exception e) {
            SimpleLogger.log("[WEATHER] Error fetching weather: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return "Error fetching weather: " + e.getMessage();
        }
    }

    private String getWeatherCondition(int code) {
        // WMO Weather interpretation codes (simplified)
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }

    private String handleReadFile(JsonObject args) {
        String filePath = args.get("file_path").getAsString();
        SimpleLogger.log("[FILE] Reading file: " + filePath);
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) return "Error: File not found: " + filePath;
            if (!Files.isRegularFile(path)) return "Error: Not a regular file: " + filePath;
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            SimpleLogger.log("[FILE] Successfully read " + Files.size(path) + " bytes from: " + filePath);
            return "File contents of " + filePath + ":\n\n" + content;
        } catch (IOException e) {
            SimpleLogger.log("[FILE] Error reading file: " + e.getMessage());
            return "Error reading file: " + e.getMessage();
        }
    }

    private String handleWriteFile(JsonObject args) {
        String filePath = args.get("file_path").getAsString();
        String content = args.get("content").getAsString();
        SimpleLogger.log("[FILE] Writing to file: " + filePath + " (" + content.length() + " bytes)");
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent(); if (parent != null && !Files.exists(parent)) { SimpleLogger.log("[FILE] Creating parent directories: " + parent); Files.createDirectories(parent); }
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            SimpleLogger.log("[FILE] Successfully wrote " + content.length() + " bytes to: " + filePath);
            return "File written successfully: " + filePath;
        } catch (IOException e) {
            SimpleLogger.log("[FILE] Error writing file: " + e.getMessage());
            return "Error writing file: " + e.getMessage();
        }
    }

    private String handleListDirectory(JsonObject args) {
        String dirPath = args.has("directory_path") ? args.get("directory_path").getAsString() : ".";
        SimpleLogger.log("[DIR] Listing directory: " + dirPath);
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) return "Error: Directory not found: " + dirPath;
            if (!Files.isDirectory(path)) return "Error: Not a directory: " + dirPath;
            StringBuilder result = new StringBuilder("Contents of " + dirPath + ":\n\n");
            List<Path> entries = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) { for (Path entry : stream) entries.add(entry); }
            entries.sort(Comparator.comparing(Path::getFileName));
            SimpleLogger.log("[DIR] Found " + entries.size() + " entries");
            for (Path entry : entries) {
                String name = entry.getFileName().toString();
                if (Files.isDirectory(entry)) result.append("[DIR]  ").append(name).append("\n"); else { long size = Files.size(entry); result.append("[FILE] ").append(name).append(" (").append(formatFileSize(size)).append(")\n"); }
            }
            return result.toString();
        } catch (IOException e) {
            SimpleLogger.log("[DIR] Error listing directory: " + e.getMessage());
            return "Error listing directory: " + e.getMessage();
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private JsonObject createTextContent(String text) {
        JsonObject content = new JsonObject(); content.addProperty("type","text"); content.addProperty("text", text); return content;
    }
}
