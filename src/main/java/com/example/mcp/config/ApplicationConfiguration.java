package com.example.mcp.config;

import com.example.mcp.adapter.in.mcp.McpServer;
import com.example.mcp.adapter.in.mcp.handler.McpPromptHandler;
import com.example.mcp.adapter.in.mcp.handler.McpResourceHandler;
import com.example.mcp.adapter.in.mcp.handler.McpToolHandler;
import com.example.mcp.adapter.out.filesystem.JavaNioFileSystemAdapter;
import com.example.mcp.adapter.out.logging.Slf4jLoggingAdapter;
import com.example.mcp.adapter.out.persistence.InMemoryNoteRepository;
import com.example.mcp.adapter.out.time.SystemTimeProvider;
import com.example.mcp.adapter.out.weather.OpenMeteoWeatherAdapter;
import com.example.mcp.application.port.out.FileSystemPort;
import com.example.mcp.application.port.out.LoggingPort;
import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.application.port.out.TimeProvider;
import com.example.mcp.application.port.out.WeatherServicePort;
import com.example.mcp.application.service.CalculationService;
import com.example.mcp.application.service.FileService;
import com.example.mcp.application.service.NoteService;
import com.example.mcp.application.service.PromptService;
import com.example.mcp.application.service.ResourceService;
import com.example.mcp.application.service.WeatherQueryService;

/**
 * Application configuration class that wires all dependencies together.
 * This is manual dependency injection - in a larger project, this could be
 * replaced with Spring, Guice, or Dagger.
 */
public final class ApplicationConfiguration {

    /**
     * Creates and configures the complete MCP server with all dependencies wired.
     * 
     * @return Fully configured McpServer ready to run
     */
    public static McpServer createMcpServer() {
        // === Infrastructure Layer (Driven Adapters / Output Ports) ===
        
        NoteRepository noteRepository = new InMemoryNoteRepository();
        WeatherServicePort weatherService = new OpenMeteoWeatherAdapter();
        FileSystemPort fileSystem = new JavaNioFileSystemAdapter();
        LoggingPort logger = new Slf4jLoggingAdapter();
        TimeProvider timeProvider = new SystemTimeProvider();

        // === Application Layer (Use Cases / Services) ===
        
        CalculationService calculationService = new CalculationService(logger);
        NoteService noteService = new NoteService(noteRepository, timeProvider, logger);
        WeatherQueryService weatherQueryService = new WeatherQueryService(weatherService, logger);
        FileService fileService = new FileService(fileSystem, logger);
        ResourceService resourceService = new ResourceService(noteRepository);
        PromptService promptService = new PromptService(noteRepository);

        // === Presentation Layer (Driving Adapters / Input Ports) ===
        
        McpToolHandler toolHandler = new McpToolHandler(
            calculationService,
            noteService,
            weatherQueryService,
            fileService
        );
        
        McpResourceHandler resourceHandler = new McpResourceHandler(resourceService);
        McpPromptHandler promptHandler = new McpPromptHandler(promptService);

        // === MCP Server ===
        
        return new McpServer(toolHandler, resourceHandler, promptHandler);
    }

    /**
     * Main entry point - creates and runs the MCP server.
     */
    public static void main(String[] args) {
        McpServer server = createMcpServer();
        server.run();
    }
}
