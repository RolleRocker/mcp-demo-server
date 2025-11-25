# MCP Demo Server - AI Agent Instructions

## Project Overview
This is a **Model Context Protocol (MCP) server** demonstrating core protocol capabilities in Java. It implements three MCP primitives: Tools (callable functions), Resources (exposed data), and Prompts (template messages).

The server communicates via **stdio transport** with JSON-RPC 2.0, allowing AI clients (like Claude Desktop) to interact with tools, read resources, and retrieve prompt templates.

## Architecture

### Core Communication Pattern
- **Transport**: stdio (reads JSON-RPC from stdin, writes responses to stdout)
- **Protocol**: JSON-RPC 2.0 with custom MCP methods
- **Main Entry**: `com.example.mcp.DemoServer` (pure Java, no external SDK)
- **JSON Library**: Gson (for serialization/deserialization)
- **Logging**: stderr (kept separate from protocol output)

### Key Files
- `src/main/java/com/example/mcp/DemoServer.java` - Main server (600+ lines, ~90% of logic)
  - `handleRequest()` - JSON-RPC method router
  - `handleInitialize()` - Protocol negotiation
  - `handleCallTool()`, `handleListTools()` - Tool execution
  - `handleReadResource()`, `handleListResources()` - Resource serving
  - `handleGetPrompt()`, `handleListPrompts()` - Prompt templates
- `pom.xml` - Maven build with shade plugin (creates fat JAR)

## Implementation Patterns

### 1. **Tools** (Interactive Functions)
Each tool has a JSON schema for input validation. Located in `handleListTools()` and called via `handleCallTool()`:

- **calculate** - arithmetic (add, subtract, multiply, divide)
- **create_note** - stores notes in `ConcurrentHashMap<Integer, Note>`
- **list_notes** - returns all notes sorted by ID
- **get_weather** - randomized simulation

**Pattern**: Tool schema must include `type`, `properties` (with descriptions), and `required` array.

### 2. **Resources** (Static/Dynamic Data)
- **Static**: `demo://info`, `demo://capabilities` (hardcoded responses)
- **Dynamic**: `note://{id}` (generated from created notes in real-time)

Resources are listed in `handleListResources()` and content served in `handleReadResource()`.

### 3. **Prompts** (Message Templates)
Three prompt templates with optional arguments:
- **helpful_assistant** - accepts `task` argument
- **code_reviewer** - accepts `language` and `code` arguments
- **summarize_notes** - no arguments, uses current note state

Pattern: Prompts return `messages` array with `role` and `content` object (type="text").

## Development Workflow

### Building
```powershell
mvn clean package
```
Creates `target/mcp-demo-server.jar` (fat JAR with all dependencies via maven-shade-plugin).

**Build Output**: Check for `BUILD SUCCESS` and verify JAR exists at `target/mcp-demo-server.jar`.

### Running Locally
```powershell
java -jar target/mcp-demo-server.jar
```
Server accepts stdin input, output goes to stdout (protocol), stderr (logs).

### Integration with Claude Desktop
Update `%APPDATA%\Claude\claude_desktop_config.json`:
```json
{
  "mcpServers": {
    "demo": {
      "command": "java",
      "args": ["-jar", "C:\\mcpDemo\\target\\mcp-demo-server.jar"]
    }
  }
}
```

## Code Organization Conventions

### Method Naming
- `handle{Action}` - JSON-RPC method handlers (e.g., `handleListTools`)
- `handleCall{Type}` - Tool-specific implementations (e.g., `handleCalculate`)

### JSON Construction
- Use `new JsonObject()` and `new JsonArray()` from Gson
- Chain `.addProperty()` for primitives, `.add()` for objects/arrays
- Convert collections to JSON with `gson.toJsonTree()`

### State Management
- `notes` (static `ConcurrentHashMap`) - thread-safe in-memory store
- `noteIdCounter` (static int) - auto-increment for note IDs
- No persistent storage (in-memory only)

## Java-Specific Requirements

- **JDK**: 21 (set in `pom.xml` `<release>` tag)
- **Java Features Used**: Streams (sorting notes), lambda expressions, generics
- **Dependencies**: Gson 2.10.1, SLF4J 2.0.9

### Maven Build Quirk
The pom.xml comment notes that `io.modelcontextprotocol:sdk-server:0.5.0` was removed because it's unavailable in Maven Central. This implementation is **standalone Java** without an official MCP SDK.

## Critical Implementation Details

1. **Error Handling in Tools**: Catch exceptions in `handleCallTool()`, return `isError: true` with error text in content.

2. **JSON-RPC Response Structure**:
   ```json
   {
     "jsonrpc": "2.0",
     "id": <request-id>,
     "result": <handler-response> OR
     "error": {"code": -32601, "message": "..."}
   }
   ```

3. **Protocol Version**: Hardcoded to `2024-11-05` in `handleInitialize()` (update if MCP spec changes).

4. **Dynamic Resources**: Always include all current notes in `handleListResources()` to reflect state changes.

5. **No Async/Multi-threading**: Single-threaded loop reads stdin, processes, writes stdout. `ConcurrentHashMap` used for safety if future features add threading.

## Common Pitfalls & How to Avoid Them

### 1. **Forgetting to Update `handleCallTool()` Switch Statement**
When adding a new tool, you must add a `case` in three places:
- `handleListTools()` - define schema and description
- `handleCallTool()` - add case to invoke handler
- Create `handleNewTool()` method to process logic

**Pitfall**: Missing any of these causes "Unknown tool" errors or silent failures.

### 2. **Incorrect JSON Schema Properties**
Each property in `inputSchema.properties` must have:
- `type` (string, number, boolean, object, array)
- `description` for client clarity
- Corresponding entry in `required` array if mandatory

**Pitfall**: Missing `description` or wrong `type` causes client confusion; missing from `required` makes parameters silently optional.

### 3. **Not Closing Resource Loops on Dynamic Data**
When resources depend on internal state (like notes), enumerate ALL current state in `handleListResources()`. If you skip this, clients won't see newly created resources.

**Pitfall**: Add a note but `resources/list` doesn't return it → client gets cached old list.

### 4. **Mixing stdout and stderr**
Only protocol JSON goes to stdout. Logs and debugging must go to stderr (e.g., `System.err.println()`).

**Pitfall**: Println to stdout corrupts the protocol stream and breaks the client connection.

### 5. **Assuming Tool Arguments Are Always Present**
Always check `.has()` before `.get()` on JsonObject, or use default values for optional arguments.

**Pitfall**: If client omits an optional arg, `args.get("key").getAsString()` throws NullPointerException.

### 6. **String Parsing Without Bounds Checking**
When parsing URIs like `note://{id}`, validate the ID exists before accessing the map.

**Pitfall**: `notes.get(invalidId)` returns null → NullPointerException on `.getTitle()`.

### 7. **Forgetting `gson.toJsonTree()` for Collections**
When adding a list to JsonObject, use `gson.toJsonTree()` to properly serialize it.

**Pitfall**: Directly calling `.add()` with a List causes type errors; `.addProperty()` doesn't work for complex types.

## Extending Tools: Code Snippet

To add a new tool (e.g., `save_file`), modify these three sections:

### 1. Add to `handleListTools()`:
```java
// In handleListTools(), add this before result.add("tools", ...):

JsonObject fileToolSchemaProp = new JsonObject();
fileToolSchemaProp.addProperty("type", "string");
fileToolSchemaProp.addProperty("description", "File path to save to");
fileProps.add("filePath", fileToolSchemaProp);

JsonObject contentToolProp = new JsonObject();
contentToolProp.addProperty("type", "string");
contentToolProp.addProperty("description", "File content");
fileProps.add("content", contentToolProp);

JsonObject saveFileTool = new JsonObject();
saveFileTool.addProperty("name", "save_file");
saveFileTool.addProperty("description", "Save content to a file on disk");
saveFileTool.add("inputSchema", fileSchema);
fileSchema.addProperty("type", "object");
fileSchema.add("properties", fileProps);
fileSchema.add("required", gson.toJsonTree(Arrays.asList("filePath", "content")));
tools.add(saveFileTool);
```

### 2. Add to `handleCallTool()` switch:
```java
case "save_file":
    content.add(createTextContent(handleSaveFile(args)));
    break;
```

### 3. Create handler method (add before helper methods):
```java
private String handleSaveFile(JsonObject args) {
    String filePath = args.get("filePath").getAsString();
    String content = args.get("content").getAsString();
    
    try {
        // Use java.nio.file.Files for safe file operations
        java.nio.file.Files.write(
            java.nio.file.Paths.get(filePath),
            content.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        return "File saved successfully to: " + filePath;
    } catch (IOException e) {
        throw new RuntimeException("Failed to save file: " + e.getMessage());
    }
}
```

The exception in the try-catch is caught by `handleCallTool()` which already sets `isError: true`.

## Testing Approach
Manual testing only (no unit tests in repo):
1. Build with `mvn clean package`
2. Configure Claude Desktop or test via command-line JSON-RPC
3. Verify tool execution, resource reading, and prompt retrieval through client interactions
