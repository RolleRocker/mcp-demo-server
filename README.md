# MCP Demo Server (Java)

A comprehensive demonstration server for the Model Context Protocol (MCP) showcasing all major capabilities, implemented in Java.

## Overview

This MCP server demonstrates the three core primitives of MCP:

- **🛠️ Tools**: Interactive functions that can be called with structured inputs
- **📄 Resources**: Exposed data sources that can be read
- **💬 Prompts**: Pre-configured prompt templates for common tasks

## Features

### Tools
- `calculate` - Perform arithmetic operations (add, subtract, multiply, divide)
- `create_note` - Create and store notes in memory
- `list_notes` - List all saved notes
- `get_weather` - Get real weather data for any city (using Open-Meteo API)
- `read_file` - Read contents of a text file
- `write_file` - Write content to a text file
- `list_directory` - List files and directories

### Resources
- `demo://info` - Server information and capabilities overview
- `demo://capabilities` - JSON overview of MCP features
- `note://{id}` - Dynamic resources for each created note

### Prompts
- `helpful_assistant` - A helpful assistant persona template
- `code_reviewer` - Code review prompt template
- `summarize_notes` - Summarize all notes in the system

## Requirements

- Java 21 or higher
- No build tool installation required (Gradle wrapper included)

## Building the Server

```bash
# On Windows
.\gradlew.bat build

# On macOS/Linux
./gradlew build
```

This will create a fat JAR file `build/libs/mcp-demo-server.jar` with all dependencies included.

## Running the Server

```bash
java -jar build/libs/mcp-demo-server.jar
```

The server runs on stdio transport, which is the standard for MCP servers.

## Configuration

To use this server with Claude Desktop or other MCP clients, add it to your configuration:

### Claude Desktop (Windows)

Edit `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "demo": {
      "command": "java",
      "args": ["-jar", "C:\\mcpDemo\\build\\libs\\mcp-demo-server.jar"]
    }
  }
}
```

### Claude Desktop (macOS/Linux)

Edit `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS) or `~/.config/claude/claude_desktop_config.json` (Linux):

```json
{
  "mcpServers": {
    "demo": {
      "command": "java",
      "args": ["-jar", "/path/to/mcpDemo/build/libs/mcp-demo-server.jar"]
    }
  }
}
```

## Usage Examples

Once configured, you can interact with the server through Claude or another MCP client:

### Using Tools
- "Calculate 45 + 67 for me"
- "Create a note titled 'Meeting Notes' with the content 'Discussed Q4 goals'"
- "List all my notes"
- "What's the weather in Tokyo?"

### Using Resources
- "Show me the server information"
- "Read the capabilities resource"
- "Show me note 1"

### Using Prompts
- "Use the helpful assistant prompt to help me plan a trip"
- "Review this Python code: [code snippet]"
- "Summarize all my notes"

## Project Structure

This project follows **Hexagonal Architecture** (also known as Ports and Adapters pattern) to achieve clean separation of concerns, testability, and maintainability.

```
mcpDemo/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── mcp/
│                       ├── config/                          # Configuration Layer
│                       │   └── ApplicationConfiguration.java # Dependency injection & wiring
│                       ├── domain/                          # Domain Layer (Business Logic)
│                       │   ├── model/                       # Domain Entities
│                       │   │   ├── Note.java
│                       │   │   ├── Calculation.java
│                       │   │   ├── Weather.java
│                       │   │   └── FileMetadata.java
│                       │   ├── service/                     # Domain Services
│                       │   │   ├── WeatherConditionInterpreter.java
│                       │   │   └── FilePathValidator.java
│                       │   └── valueobject/                 # Value Objects
│                       │       ├── NoteId.java
│                       │       ├── Operation.java
│                       │       ├── Temperature.java
│                       │       ├── WindSpeed.java
│                       │       ├── Coordinates.java
│                       │       ├── CityName.java
│                       │       ├── FilePath.java
│                       │       └── FileSize.java
│                       ├── application/                     # Application Layer (Use Cases)
│                       │   ├── port/                       
│                       │   │   ├── in/                     # Input Ports (Use Case Interfaces)
│                       │   │   │   ├── CalculationUseCase.java
│                       │   │   │   ├── NoteManagementUseCase.java
│                       │   │   │   ├── WeatherQueryUseCase.java
│                       │   │   │   ├── FileOperationUseCase.java
│                       │   │   │   ├── ResourceQueryUseCase.java
│                       │   │   │   └── PromptGenerationUseCase.java
│                       │   │   └── out/                    # Output Ports (Repository/Service Interfaces)
│                       │   │       ├── NoteRepository.java
│                       │   │       ├── WeatherServicePort.java
│                       │   │       ├── FileSystemPort.java
│                       │   │       ├── LoggingPort.java
│                       │   │       └── TimeProvider.java
│                       │   └── service/                    # Application Services (Use Case Implementations)
│                       │       ├── CalculationService.java
│                       │       ├── NoteService.java
│                       │       ├── WeatherQueryService.java
│                       │       ├── FileService.java
│                       │       ├── ResourceService.java
│                       │       └── PromptService.java
│                       └── adapter/                        # Adapter Layer
│                           ├── in/                        # Driving Adapters (Primary)
│                           │   └── mcp/                   # MCP Protocol Handlers
│                           │       ├── McpServer.java
│                           │       ├── McpToolHandler.java
│                           │       ├── McpResourceHandler.java
│                           │       └── McpPromptHandler.java
│                           └── out/                       # Driven Adapters (Secondary/Infrastructure)
│                               ├── InMemoryNoteRepository.java
│                               ├── OpenMeteoWeatherAdapter.java
│                               ├── JavaNioFileSystemAdapter.java
│                               ├── Slf4jLoggingAdapter.java
│                               └── SystemTimeProvider.java
├── build.gradle
├── settings.gradle
├── gradlew (Unix wrapper)
├── gradlew.bat (Windows wrapper)
└── README.md
```

### Architecture Layers

#### 1. **Domain Layer** (`domain/`)
Pure business logic with **zero** dependencies on frameworks or infrastructure:
- **Entities**: Rich domain objects with business rules (Note, Calculation, Weather, FileMetadata)
- **Value Objects**: Immutable types with validation (NoteId, Temperature, Coordinates, etc.)
- **Domain Services**: Business logic that doesn't belong to a single entity

#### 2. **Application Layer** (`application/`)
Orchestrates domain logic to implement use cases:
- **Input Ports** (`port/in/`): Interfaces defining use cases (what the application can do)
- **Output Ports** (`port/out/`): Interfaces for external dependencies (repositories, services)
- **Services** (`service/`): Implementations of use cases, coordinating domain objects

#### 3. **Adapter Layer** (`adapter/`)
Connects the application to the outside world:
- **Driving Adapters** (`in/`): MCP protocol handlers that invoke use cases
- **Driven Adapters** (`out/`): Infrastructure implementations (database, HTTP clients, file system, logging)

#### 4. **Configuration Layer** (`config/`)
Wires everything together with dependency injection (manual DI, no Spring required)

### Dependency Rule

Dependencies flow **inward only**:
```
Adapters → Application → Domain
```

- Domain has NO dependencies (pure business logic)
- Application depends ONLY on Domain (via ports/interfaces)
- Adapters depend on Application and Domain (implement ports, call use cases)

### Benefits of This Architecture

✅ **Testability**: Each layer can be tested independently with mocks/stubs  
✅ **Maintainability**: Clear separation of concerns, easy to understand and modify  
✅ **Flexibility**: Swap implementations without changing business logic (e.g., replace InMemory with SQL)  
✅ **Independence**: Domain logic isolated from frameworks, databases, and protocols  
✅ **Clean Code**: Following SOLID principles and Domain-Driven Design patterns



## Technical Details

- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Protocol**: Model Context Protocol (MCP)
- **Transport**: stdio
- **Runtime**: Java 21
- **Build Tool**: Gradle 8.5 with Shadow plugin
- **JSON Library**: Gson 2.10.1
- **Logging**: SLF4J 2.0.9 with file persistence
- **HTTP Client**: Java 21 built-in HttpClient
- **Weather API**: Open-Meteo (free, no API key required)
- **Implementation**: Pure Java with JSON-RPC over stdio

## Implementation Notes

This is a pure Java implementation that communicates via JSON-RPC over stdio. Since there isn't an official Java SDK for MCP yet, this implementation:

1. Reads JSON-RPC requests from stdin
2. Routes requests to appropriate handlers (tools, resources, prompts)
3. Delegates to application services (use cases) via port interfaces
4. Returns JSON-RPC responses to stdout
5. Logs to stderr and `mcp-demo-server.log` (not stdout to avoid interfering with protocol)

The architecture follows hexagonal principles:
- **Domain layer**: Pure business logic, framework-agnostic
- **Application layer**: Use cases orchestrating domain logic
- **Adapter layer**: MCP protocol handling and infrastructure implementations
- **Manual dependency injection**: No framework required, explicit wiring in `ApplicationConfiguration`

The server is fully functional, production-ready, and compatible with MCP clients like Claude Desktop.

## Learn More

- [MCP Documentation](https://modelcontextprotocol.io)
- [MCP Specification](https://spec.modelcontextprotocol.io)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)

## License

MIT
