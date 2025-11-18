#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  ListResourcesRequestSchema,
  ReadResourceRequestSchema,
  ListPromptsRequestSchema,
  GetPromptRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

// Sample data store for demo purposes
const notes = new Map();
let noteIdCounter = 1;

// Create MCP server instance
const server = new Server(
  {
    name: "mcp-demo-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
      resources: {},
      prompts: {},
    },
  }
);

// ============================================================================
// TOOLS - Demonstrate tool calling capabilities
// ============================================================================

server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "calculate",
        description: "Perform basic arithmetic calculations (add, subtract, multiply, divide)",
        inputSchema: {
          type: "object",
          properties: {
            operation: {
              type: "string",
              enum: ["add", "subtract", "multiply", "divide"],
              description: "The arithmetic operation to perform",
            },
            a: {
              type: "number",
              description: "First number",
            },
            b: {
              type: "number",
              description: "Second number",
            },
          },
          required: ["operation", "a", "b"],
        },
      },
      {
        name: "create_note",
        description: "Create a new note with a title and content",
        inputSchema: {
          type: "object",
          properties: {
            title: {
              type: "string",
              description: "The title of the note",
            },
            content: {
              type: "string",
              description: "The content of the note",
            },
          },
          required: ["title", "content"],
        },
      },
      {
        name: "list_notes",
        description: "List all notes with their IDs and titles",
        inputSchema: {
          type: "object",
          properties: {},
        },
      },
      {
        name: "get_weather",
        description: "Get simulated weather information for a city",
        inputSchema: {
          type: "object",
          properties: {
            city: {
              type: "string",
              description: "The city name",
            },
          },
          required: ["city"],
        },
      },
    ],
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  switch (name) {
    case "calculate": {
      const { operation, a, b } = args;
      let result;
      
      switch (operation) {
        case "add":
          result = a + b;
          break;
        case "subtract":
          result = a - b;
          break;
        case "multiply":
          result = a * b;
          break;
        case "divide":
          if (b === 0) {
            throw new Error("Division by zero is not allowed");
          }
          result = a / b;
          break;
        default:
          throw new Error(`Unknown operation: ${operation}`);
      }
      
      return {
        content: [
          {
            type: "text",
            text: `Result: ${a} ${operation === "add" ? "+" : operation === "subtract" ? "-" : operation === "multiply" ? "×" : "÷"} ${b} = ${result}`,
          },
        ],
      };
    }

    case "create_note": {
      const { title, content } = args;
      const id = noteIdCounter++;
      notes.set(id, { id, title, content, created: new Date().toISOString() });
      
      return {
        content: [
          {
            type: "text",
            text: `Note created successfully!\nID: ${id}\nTitle: ${title}`,
          },
        ],
      };
    }

    case "list_notes": {
      if (notes.size === 0) {
        return {
          content: [
            {
              type: "text",
              text: "No notes found. Create one using the create_note tool!",
            },
          ],
        };
      }
      
      const notesList = Array.from(notes.values())
        .map((note) => `ID ${note.id}: ${note.title}`)
        .join("\n");
      
      return {
        content: [
          {
            type: "text",
            text: `Available notes (${notes.size}):\n${notesList}`,
          },
        ],
      };
    }

    case "get_weather": {
      const { city } = args;
      
      // Simulated weather data
      const conditions = ["Sunny", "Cloudy", "Rainy", "Partly Cloudy"];
      const condition = conditions[Math.floor(Math.random() * conditions.length)];
      const temp = Math.floor(Math.random() * 30) + 10;
      
      return {
        content: [
          {
            type: "text",
            text: `Weather in ${city}:\n🌡️ Temperature: ${temp}°C\n☁️ Condition: ${condition}\n💨 Wind: ${Math.floor(Math.random() * 20)} km/h`,
          },
        ],
      };
    }

    default:
      throw new Error(`Unknown tool: ${name}`);
  }
});

// ============================================================================
// RESOURCES - Demonstrate resource exposure
// ============================================================================

server.setRequestHandler(ListResourcesRequestSchema, async () => {
  const resources = [
    {
      uri: "demo://info",
      mimeType: "text/plain",
      name: "Server Information",
      description: "Information about this MCP demo server",
    },
    {
      uri: "demo://capabilities",
      mimeType: "application/json",
      name: "MCP Capabilities",
      description: "Overview of MCP protocol capabilities",
    },
  ];

  // Add dynamic resources for each note
  for (const [id, note] of notes) {
    resources.push({
      uri: `note://${id}`,
      mimeType: "text/plain",
      name: `Note: ${note.title}`,
      description: `Note created on ${note.created}`,
    });
  }

  return { resources };
});

server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
  const { uri } = request.params;

  if (uri === "demo://info") {
    return {
      contents: [
        {
          uri,
          mimeType: "text/plain",
          text: `MCP Demo Server v1.0.0

This server demonstrates the core capabilities of the Model Context Protocol:

🛠️  TOOLS: Interactive functions that can be called
   - calculate: Perform arithmetic operations
   - create_note: Create and store notes
   - list_notes: View all saved notes
   - get_weather: Get simulated weather data

📄 RESOURCES: Exposed data that can be read
   - Server information (this document)
   - Capabilities overview
   - Dynamic note resources

💬 PROMPTS: Pre-configured prompt templates
   - Helpful assistant persona
   - Code review assistant
   - Note summarizer

The MCP allows AI models to interact with external tools and data sources in a standardized way.`,
        },
      ],
    };
  }

  if (uri === "demo://capabilities") {
    const capabilities = {
      protocol: "Model Context Protocol (MCP)",
      version: "1.0.0",
      features: {
        tools: "Execute functions with structured input/output",
        resources: "Access and read external data sources",
        prompts: "Use pre-configured prompt templates",
      },
      transport: "stdio",
      documentation: "https://modelcontextprotocol.io",
    };

    return {
      contents: [
        {
          uri,
          mimeType: "application/json",
          text: JSON.stringify(capabilities, null, 2),
        },
      ],
    };
  }

  // Handle note resources
  if (uri.startsWith("note://")) {
    const id = parseInt(uri.replace("note://", ""));
    const note = notes.get(id);

    if (!note) {
      throw new Error(`Note not found: ${id}`);
    }

    return {
      contents: [
        {
          uri,
          mimeType: "text/plain",
          text: `Title: ${note.title}\nCreated: ${note.created}\n\n${note.content}`,
        },
      ],
    };
  }

  throw new Error(`Unknown resource: ${uri}`);
});

// ============================================================================
// PROMPTS - Demonstrate prompt templates
// ============================================================================

server.setRequestHandler(ListPromptsRequestSchema, async () => {
  return {
    prompts: [
      {
        name: "helpful_assistant",
        description: "A helpful and friendly assistant persona",
        arguments: [
          {
            name: "task",
            description: "The task to help with",
            required: true,
          },
        ],
      },
      {
        name: "code_reviewer",
        description: "Review code and provide constructive feedback",
        arguments: [
          {
            name: "language",
            description: "Programming language",
            required: true,
          },
          {
            name: "code",
            description: "Code to review",
            required: true,
          },
        ],
      },
      {
        name: "summarize_notes",
        description: "Summarize all notes in the system",
        arguments: [],
      },
    ],
  };
});

server.setRequestHandler(GetPromptRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  switch (name) {
    case "helpful_assistant": {
      const task = args?.task || "general assistance";
      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `You are a helpful, friendly, and knowledgeable assistant. Please help me with the following task:\n\n${task}\n\nProvide clear, accurate, and actionable guidance.`,
            },
          },
        ],
      };
    }

    case "code_reviewer": {
      const language = args?.language || "unknown";
      const code = args?.code || "";
      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `Please review the following ${language} code and provide constructive feedback:\n\n\`\`\`${language}\n${code}\n\`\`\`\n\nConsider:\n- Code quality and readability\n- Potential bugs or issues\n- Performance concerns\n- Best practices\n- Suggestions for improvement`,
            },
          },
        ],
      };
    }

    case "summarize_notes": {
      if (notes.size === 0) {
        return {
          messages: [
            {
              role: "user",
              content: {
                type: "text",
                text: "There are no notes to summarize. Please create some notes first using the create_note tool.",
              },
            },
          ],
        };
      }

      const notesText = Array.from(notes.values())
        .map((note) => `**${note.title}** (ID: ${note.id})\n${note.content}`)
        .join("\n\n---\n\n");

      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `Please provide a concise summary of the following notes:\n\n${notesText}`,
            },
          },
        ],
      };
    }

    default:
      throw new Error(`Unknown prompt: ${name}`);
  }
});

// ============================================================================
// START SERVER
// ============================================================================

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("MCP Demo Server running on stdio");
}

main().catch((error) => {
  console.error("Server error:", error);
  process.exit(1);
});
