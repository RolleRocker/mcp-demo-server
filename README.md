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

```
mcpDemo/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── mcp/
│                       ├── Main.java
│                       ├── ToolsManager.java
│                       ├── ResourceManager.java
│                       ├── PromptManager.java
│                       └── Note.java
├── build.gradle
├── settings.gradle
├── gradlew (Unix wrapper)
├── gradlew.bat (Windows wrapper)
└── README.md
```

**Note:** This repository previously included Node/JavaScript artifacts (for example `index.js`, `package.json`, and a `node_modules/` folder). Those files have been removed to keep the project focused on the Java MCP demo server.

## Technical Details

- **Protocol**: Model Context Protocol (MCP)
- **Transport**: stdio
- **Runtime**: Java 21
- **Build Tool**: Gradle 8.5 with Shadow plugin
- **JSON Library**: Gson 2.10.1
- **Logging**: SLF4J 2.0.9
- **Implementation**: Pure Java with JSON-RPC over stdio

## Implementation Notes

This is a pure Java implementation that communicates via JSON-RPC over stdio. Since there isn't an official Java SDK for MCP yet, this implementation:

1. Reads JSON-RPC requests from stdin
2. Processes requests according to MCP specification
3. Returns JSON-RPC responses to stdout
4. Logs to stderr (not stdout to avoid interfering with protocol)

The server is fully functional and compatible with MCP clients like Claude Desktop.

## Learn More

- [MCP Documentation](https://modelcontextprotocol.io)
- [MCP Specification](https://spec.modelcontextprotocol.io)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)

## License

MIT
