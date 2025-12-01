# MCP Demo Server - AI Agent Instructions

## Project Overview
This is a **Model Context Protocol (MCP) server** demonstrating core protocol capabilities in Java. It implements three MCP primitives: Tools (callable functions), Resources (exposed data), and Prompts (template messages).

The server communicates via **stdio transport** with JSON-RPC 2.0, allowing AI clients (like Claude Desktop) to interact with tools, read resources, and retrieve prompt templates.

## Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters pattern) with clean separation between domain logic, application use cases, and infrastructure adapters.

### Core Communication Pattern
- **Transport**: stdio (reads JSON-RPC from stdin, writes responses to stdout)
- **Protocol**: JSON-RPC 2.0 with custom MCP methods
- **Main Entry**: `com.example.mcp.config.ApplicationConfiguration` (dependency injection and main entry point)
- **JSON Library**: Gson (for serialization/deserialization)
- **Logging**: stderr + file persistence to `mcp-demo-server.log` (kept separate from protocol output)

### Architecture Layers

#### 1. Domain Layer (`domain/`)
Pure business logic with zero framework dependencies:
- **Entities**: `Note`, `Calculation`, `Weather`, `FileMetadata`
- **Value Objects**: `NoteId`, `Operation`, `Temperature`, `WindSpeed`, `Coordinates`, `CityName`, `FilePath`, `FileSize`
- **Domain Services**: `WeatherConditionInterpreter`, `FilePathValidator`

#### 2. Application Layer (`application/`)
Use cases orchestrating domain logic:
- **Input Ports** (`port/in/`): `CalculationUseCase`, `NoteManagementUseCase`, `WeatherQueryUseCase`, `FileOperationUseCase`, `ResourceQueryUseCase`, `PromptGenerationUseCase`
- **Output Ports** (`port/out/`): `NoteRepository`, `WeatherServicePort`, `FileSystemPort`, `LoggingPort`, `TimeProvider`
- **Services** (`service/`): Implementations of use cases

#### 3. Adapter Layer (`adapter/`)
- **Driving Adapters** (`in/mcp/`): `McpServer`, `McpToolHandler`, `McpResourceHandler`, `McpPromptHandler`
- **Driven Adapters** (`out/`): `InMemoryNoteRepository`, `OpenMeteoWeatherAdapter`, `JavaNioFileSystemAdapter`, `Slf4jLoggingAdapter`, `SystemTimeProvider`

#### 4. Configuration Layer (`config/`)
- `ApplicationConfiguration` - Manual dependency injection, wires all components

### Key Files
- `src/main/java/com/example/mcp/config/ApplicationConfiguration.java` - Entry point with dependency injection
- `src/main/java/com/example/mcp/adapter/in/mcp/McpServer.java` - Main protocol server (~130 lines)
  - Reads JSON-RPC from stdin, routes to handlers
  - Delegates to `McpToolHandler`, `McpResourceHandler`, `McpPromptHandler`
- `src/main/java/com/example/mcp/adapter/in/mcp/handler/McpToolHandler.java` - Tool routing (~400 lines)
  - Maps tool calls to use cases
  - Handles 7 tools: calculate, create_note, list_notes, get_weather, read_file, write_file, list_directory
- `build.gradle` - Gradle build with Shadow plugin (creates fat JAR)
- `pom.xml` - Maven configuration (kept for compatibility)

## Implementation Patterns

### 1. **Tools** (Interactive Functions)
Each tool has a JSON schema for input validation. The architecture separates:
- **Tool definitions** in `McpToolHandler.listTools()` - JSON schemas
- **Tool execution** in `McpToolHandler.handleToolCall()` - routes to use cases
- **Business logic** in application services (CalculationService, NoteService, etc.)
- **Domain logic** in domain entities (Calculation.perform(), Note validation, etc.)

**Available Tools:**
- **calculate** - arithmetic (add, subtract, multiply, divide) via `CalculationUseCase`
- **create_note** - stores notes via `NoteManagementUseCase` → `NoteRepository`
- **list_notes** - returns all notes sorted by ID
- **get_weather** - real weather data via `WeatherQueryUseCase` → `WeatherServicePort` (Open-Meteo API)
- **read_file** - file reading via `FileOperationUseCase` → `FileSystemPort`
- **write_file** - file writing with validation via `FilePathValidator`
- **list_directory** - directory listing

**Pattern**: Tool schema must include `type`, `properties` (with descriptions), and `required` array.

### 2. **Resources** (Static/Dynamic Data)
Handled by `ResourceService` implementing `ResourceQueryUseCase`:
- **Static**: `demo://info`, `demo://capabilities` (hardcoded responses)
- **Dynamic**: `note://{id}` (generated from notes in repository)

Resources are listed via `ResourceQueryUseCase.listResources()` and read via `ResourceQueryUseCase.readResource(uri)`.
## Development Workflow

### Building
```powershell
# Using Gradle (recommended)
.\gradlew.bat build

# Or using Maven (legacy support)
mvn clean package
```
Creates fat JAR at:
- **Gradle**: `build/libs/mcp-demo-server.jar` (via Shadow plugin)
- **Maven**: `target/mcp-demo-server.jar` (via Shade plugin)

**Build Output**: Check for `BUILD SUCCESS` and verify JAR exists.

### Running Locally
```powershell
# Using Gradle-built JAR (recommended)
java -jar build\libs\mcp-demo-server.jar

# Or using Maven-built JAR
java -jar target\mcp-demo-server.jar
```
Server accepts stdin input, output goes to stdout (protocol), stderr + file (logs to `mcp-demo-server.log`).

### Testing
```powershell
# Run all tests
.\gradlew.bat test

# Run integration smoke test
.\test\integration\run_stateful_smoke.ps1
```

### Integration with Claude Desktop
Update `%APPDATA%\Claude\claude_desktop_config.json`:
```json
{
  "mcpServers": {
    "demo": {
      "command": "java",
      "args": ["-jar", "C:\\mcpDemo\\build\\libs\\mcp-demo-server.jar"]
    }
  }
}
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

### Package Structure
```
com.example.mcp/
├── config/                          # Configuration & DI
│   └── ApplicationConfiguration.java
├── domain/                          # Pure business logic
│   ├── model/                       # Entities
│   ├── valueobject/                 # Value objects
│   └── service/                     # Domain services
├── application/                     # Use cases
│   ├── port/in/                     # Input ports (use case interfaces)
│   ├── port/out/                    # Output ports (infrastructure interfaces)
│   └── service/                     # Use case implementations
└── adapter/                         # Infrastructure & protocol
## Java-Specific Requirements

- **JDK**: 21 (set in both `build.gradle` and `pom.xml`)
- **Java Features Used**: Records, streams, lambdas, pattern matching, switch expressions
- **Dependencies**: 
  - Gson 2.10.1 (JSON processing, used only in adapters)
  - SLF4J Simple 2.0.9 (logging abstraction)
  - JUnit Jupiter 5.10.0 (testing)
  - Java 21 HttpClient (built-in, used in `OpenMeteoWeatherAdapter`)

### Build System
- **Primary**: Gradle 8.5 with Shadow plugin
- **Legacy**: Maven with Shade plugin (kept for compatibility)
- Both produce fat JARs with all dependencies included

### Architecture Note
This is a **pure Java implementation** without an official MCP SDK. The MCP protocol is implemented manually using JSON-RPC over stdio. The hexagonal architecture ensures that protocol handling is isolated in adapters, making it easy to add other protocols (REST, gRPC) in the future.

### JSON Construction
- Use `new JsonObject()` and `new JsonArray()` from Gson in adapters only
- Chain `.addProperty()` for primitives, `.add()` for objects/arrays
- Convert collections to JSON with `gson.toJsonTree()`
- **Keep JSON handling in adapters** - domain and application use POJOs/records

### State Management
- Notes stored via `NoteRepository` interface (implementation: `InMemoryNoteRepository` with `ConcurrentHashMap`)
- Weather data fetched via `WeatherServicePort` interface (implementation: `OpenMeteoWeatherAdapter`)
- File operations via `FileSystemPort` interface (implementation: `JavaNioFileSystemAdapter`)
- Time via `TimeProvider` interface (implementation: `SystemTimeProvider` - for testability)
- Logging via `LoggingPort` interface (implementation: `Slf4jLoggingAdapter`)

### Dependency Direction
**Critical Rule**: Dependencies flow inward only!
```
Adapters → Application → Domain
```
- **Domain** has NO dependencies (no Gson, no HTTP, no file I/O, no logging)
- **Application** depends only on domain types and port interfaces
- **Adapters** implement ports and depend on application/domain

## Java-Specific Requirements

- **JDK**: 21 (set in `pom.xml` `<release>` tag)
- **Java Features Used**: Streams (sorting notes), lambda expressions, generics
- **Dependencies**: Gson 2.10.1, SLF4J 2.0.9

### Maven Build Quirk
The pom.xml comment notes that `io.modelcontextprotocol:sdk-server:0.5.0` was removed because it's unavailable in Maven Central. This implementation is **standalone Java** without an official MCP SDK.

## Critical Implementation Details

1. **Error Handling**: Exceptions in use cases are caught by MCP handlers, converted to `isError: true` responses with error text.

2. **JSON-RPC Response Structure** (handled in `McpServer`):
   ```json
   {
     "jsonrpc": "2.0",
     "id": <request-id>,
     "result": <handler-response> OR
     "error": {"code": -32601, "message": "..."}
   }
   ```

3. **Protocol Version**: Hardcoded to `2024-11-05` in `McpServer.handleInitialize()` (update if MCP spec changes).

4. **Dynamic Resources**: `ResourceService` generates note resources dynamically from `NoteRepository`.

5. **Hexagonal Benefits**: 
   - All business logic testable without infrastructure
   - Swap implementations without changing core logic (e.g., replace `InMemoryNoteRepository` with SQL)
   - Add new protocols (REST API) by adding new driving adapters
   - Mock all external dependencies via port interfaces

## Common Pitfalls & How to Avoid Them

### 1. **Breaking Hexagonal Architecture Dependency Rule**
**Rule**: Dependencies must flow inward (Adapters → Application → Domain)

**Pitfall**: Adding infrastructure dependencies to domain (e.g., Gson, HttpClient in entities)

**Solution**: Keep domain pure. Use value objects and entities with no framework dependencies. Put all infrastructure code in adapters.

### 2. **Adding New Tools Without Following the Pattern**
When adding a new tool, you must:
1. Define use case interface in `application/port/in/`
2. Implement use case in `application/service/`
3. Add tool schema in `McpToolHandler.listTools()`
4. Add tool handler in `McpToolHandler.handleToolCall()`
5. Wire dependencies in `ApplicationConfiguration`

**Pitfall**: Skipping any step causes compilation errors or runtime failures.

### 3. **Incorrect JSON Schema Properties**
Each property in `inputSchema.properties` must have:
- `type` (string, number, boolean, object, array)
- `description` for client clarity
- Corresponding entry in `required` array if mandatory

**Pitfall**: Missing `description` or wrong `type` causes client confusion.

### 4. **Mixing Protocol Logic with Business Logic**
**Pitfall**: Putting business rules in MCP handlers instead of domain/application layers.

**Solution**: MCP handlers should only:
- Parse JSON
- Call use cases
- Format responses
All business logic belongs in domain entities or application services.

### 5. **Not Updating Dynamic Resources**
**Pitfall**: Forgetting to enumerate state changes in `ResourceService.listResources()`.

**Solution**: `ResourceService` queries `NoteRepository` to generate current note resources dynamically.

### 6. **Mixing stdout and stderr**
Only protocol JSON goes to stdout. Logs must go to stderr or file.

**Pitfall**: Using `System.out.println()` corrupts the protocol stream.

**Solution**: Use `LoggingPort` interface (implemented by `Slf4jLoggingAdapter`).

### 7. **Assuming Tool Arguments Are Always Present**
Always validate arguments or use Optional types.

**Pitfall**: `args.get("key").getAsString()` throws NullPointerException if key missing.

**Solution**: Check with `.has()` first, or handle in use case with validation.

### 8. **Forgetting to Wire Dependencies in ApplicationConfiguration**
**Pitfall**: Creating new services but not adding them to dependency graph in `ApplicationConfiguration.main()`.

**Solution**: All components must be instantiated and wired in `ApplicationConfiguration`.

## Extending Tools: Code Snippet

To add a new tool following hexagonal architecture:

### 1. Define Use Case Interface (`application/port/in/NewFeatureUseCase.java`):
```java
package com.example.mcp.application.port.in;

public interface NewFeatureUseCase {
    String executeFeature(String input);
}
```

### 2. Implement Use Case (`application/service/NewFeatureService.java`):
```java
package com.example.mcp.application.service;

import com.example.mcp.application.port.in.NewFeatureUseCase;

public class NewFeatureService implements NewFeatureUseCase {
    @Override
    public String executeFeature(String input) {
        // Business logic here (delegate to domain if needed)
        return "Feature executed: " + input;
    }
}
```

### 3. Add to `McpToolHandler.listTools()`:
```java
// In McpToolHandler.listTools(), add this to the tools array:
JsonObject newToolProps = new JsonObject();
JsonObject inputProp = new JsonObject();
inputProp.addProperty("type", "string");
inputProp.addProperty("description", "Input for the feature");
newToolProps.add("input", inputProp);

JsonObject newToolSchema = new JsonObject();
newToolSchema.addProperty("type", "object");
newToolSchema.add("properties", newToolProps);
newToolSchema.add("required", gson.toJsonTree(List.of("input")));

JsonObject newTool = new JsonObject();
newTool.addProperty("name", "new_feature");
newTool.addProperty("description", "Execute new feature");
newTool.add("inputSchema", newToolSchema);
tools.add(newTool);
```

### 4. Add to `McpToolHandler.handleToolCall()` switch:
```java
case "new_feature":
    String input = args.get("input").getAsString();
    String result = newFeatureUseCase.executeFeature(input);
    content.add(createTextContent(result));
    break;
```

### 5. Wire in `ApplicationConfiguration.main()`:
```java
// Add to ApplicationConfiguration.main():
NewFeatureUseCase newFeatureUseCase = new NewFeatureService();

// Update McpToolHandler constructor call to include new use case:
McpToolHandler toolHandler = new McpToolHandler(
    calculationUseCase,
    noteManagementUseCase,
    weatherQueryUseCase,
    fileOperationUseCase,
    newFeatureUseCase  // Add here
);
```

This pattern maintains clean architecture: domain logic → use case → adapter → protocol.

## Testing Approach

### Current Testing
- **Integration Test**: `IntegrationJvmTest.java` - Calls `ApplicationConfiguration.main()` with piped stdin/stdout
- **Smoke Test**: `test/integration/run_stateful_smoke.ps1` - 10 JSON-RPC requests testing tools, resources, prompts

### Testing Strategy by Layer

#### Domain Layer Tests (Unit Tests)
Test pure business logic:
- Value object validation (Temperature >= absolute zero, NoteId > 0)
- Entity business rules (Note title length, Calculation operations)
- Domain services (weather code interpretation, path validation)
- **No mocks needed** - pure Java, no dependencies

#### Application Layer Tests (Service Tests)
Test use case implementations with mocked ports:
- Mock `NoteRepository` to test `NoteService`
- Mock `WeatherServicePort` to test `WeatherQueryService`
- Mock `FileSystemPort` to test `FileService`
- Verify use case orchestration logic

#### Adapter Layer Tests (Integration Tests)
Test infrastructure implementations:
- `InMemoryNoteRepository` - verify ConcurrentHashMap operations
- `OpenMeteoWeatherAdapter` - test with real API or mock HTTP responses
- `JavaNioFileSystemAdapter` - test with temporary directories
- `McpToolHandler` - test JSON schema generation and tool routing

### Running Tests
```powershell
# All tests
.\gradlew.bat test

# Integration smoke test
.\test\integration\run_stateful_smoke.ps1

# Manual protocol test
echo '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}' | java -jar build\libs\mcp-demo-server.jar
```
