# Hexagonal Architecture Refactoring Plan

## Executive Summary

This document outlines the complete refactoring plan to transform the MCP Demo Server from its current layered architecture into a hexagonal architecture (Ports & Adapters pattern). This will improve testability, maintainability, and separation of concerns.

---

## Current Architecture Analysis

### Problems Identified

1. **Mixed Concerns**: Manager classes (`ToolsManager`, `ResourceManager`, `PromptManager`) combine:
   - MCP protocol handling (JSON-RPC)
   - Business logic (calculations, note management)
   - Infrastructure concerns (HTTP calls, file I/O)

2. **Tight Coupling**: 
   - Direct dependency on Gson for JSON handling throughout
   - Direct HTTP client usage in `ToolsManager`
   - Direct file system access via `java.nio.file`
   - Hard-coded weather API URLs

3. **Testability Issues**:
   - Cannot test business logic without MCP protocol
   - Cannot mock external dependencies (weather API, file system)
   - Shared mutable state (`ConcurrentHashMap<Integer, Note>`)

4. **Inflexibility**:
   - Cannot swap storage implementations (memory → database)
   - Cannot swap weather providers without code changes
   - Cannot reuse business logic in different contexts (REST API, CLI, etc.)

### Current Structure
```
com.example.mcp/
├── Main.java                  // Entry point + JSON-RPC router
├── ToolsManager.java          // Tools + business logic + infrastructure
├── ResourceManager.java       // Resources + MCP protocol
├── PromptManager.java         // Prompts + MCP protocol
├── Note.java                  // Simple data holder
└── SimpleLogger.java          // Logging wrapper
```

---

## Target Hexagonal Architecture

### Conceptual Model

```
┌─────────────────────────────────────────────────────────────┐
│                    DRIVING ADAPTERS                          │
│  (Primary/Input - who uses the application)                  │
│                                                               │
│  ┌─────────────────────────────────────────────────┐        │
│  │  MCP Protocol Adapter (JSON-RPC over stdio)     │        │
│  │  - McpServer                                     │        │
│  │  - McpToolHandler                                │        │
│  │  - McpResourceHandler                            │        │
│  │  - McpPromptHandler                              │        │
│  └─────────────────────────────────────────────────┘        │
│                           │                                   │
└───────────────────────────┼───────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                          │
│              (Use Cases - Business Rules)                    │
│                                                               │
│  Input Ports (Interfaces):                                   │
│  ┌──────────────────────────────────────────────┐           │
│  │ CalculationUseCase                            │           │
│  │ NoteManagementUseCase                         │           │
│  │ WeatherQueryUseCase                           │           │
│  │ FileOperationUseCase                          │           │
│  │ ResourceQueryUseCase                          │           │
│  │ PromptGenerationUseCase                       │           │
│  └──────────────────────────────────────────────┘           │
│                                                               │
│  Implementations:                                             │
│  ┌──────────────────────────────────────────────┐           │
│  │ CalculationService                            │           │
│  │ NoteService                                   │           │
│  │ WeatherQueryService                           │           │
│  │ FileService                                   │           │
│  │ ResourceService                               │           │
│  │ PromptService                                 │           │
│  └──────────────────────────────────────────────┘           │
│                           │                                   │
└───────────────────────────┼───────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│                  (Pure Business Logic)                       │
│                                                               │
│  Entities:                                                    │
│  ┌──────────────────────────────────────────────┐           │
│  │ Note (rich domain model)                      │           │
│  │ Calculation                                   │           │
│  │ Weather                                       │           │
│  │ FileMetadata                                  │           │
│  └──────────────────────────────────────────────┘           │
│                                                               │
│  Value Objects:                                               │
│  ┌──────────────────────────────────────────────┐           │
│  │ NoteId, Temperature, WindSpeed                │           │
│  │ FilePath, FileSize, Operation                 │           │
│  │ Coordinates, CityName                         │           │
│  └──────────────────────────────────────────────┘           │
│                                                               │
│  Domain Services (optional):                                  │
│  ┌──────────────────────────────────────────────┐           │
│  │ WeatherConditionInterpreter                   │           │
│  │ FilePathValidator                             │           │
│  └──────────────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────┘
                            ▲
                            │
┌───────────────────────────┼───────────────────────────────────┐
│                   APPLICATION LAYER                          │
│                  (Output Ports - Interfaces)                 │
│                                                               │
│  ┌──────────────────────────────────────────────┐           │
│  │ NoteRepository                                │           │
│  │ WeatherServicePort                            │           │
│  │ FileSystemPort                                │           │
│  │ LoggingPort                                   │           │
│  │ TimeProvider                                  │           │
│  └──────────────────────────────────────────────┘           │
│                           │                                   │
└───────────────────────────┼───────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    DRIVEN ADAPTERS                           │
│  (Secondary/Output - what the application uses)              │
│                                                               │
│  ┌─────────────────────────────────────────────────┐        │
│  │  Infrastructure Implementations:                 │        │
│  │  - InMemoryNoteRepository                        │        │
│  │  - OpenMeteoWeatherAdapter                       │        │
│  │  - JavaNioFileSystemAdapter                      │        │
│  │  - Slf4jLoggingAdapter                           │        │
│  │  - SystemTimeProvider                            │        │
│  └─────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

---

## Detailed Implementation Plan

### Phase 1: Domain Layer (Core Business Logic)

#### 1.1 Create Package Structure
```
src/main/java/com/example/mcp/
├── domain/
│   ├── model/
│   │   ├── Note.java
│   │   ├── Calculation.java
│   │   ├── Weather.java
│   │   └── FileMetadata.java
│   ├── valueobject/
│   │   ├── NoteId.java
│   │   ├── Temperature.java
│   │   ├── WindSpeed.java
│   │   ├── Coordinates.java
│   │   ├── CityName.java
│   │   ├── FilePath.java
│   │   ├── FileSize.java
│   │   └── Operation.java
│   └── service/
│       ├── WeatherConditionInterpreter.java
│       └── FilePathValidator.java
```

#### 1.2 Domain Entities

**Note.java** (Enhanced)
```java
package com.example.mcp.domain.model;

import com.example.mcp.domain.valueobject.NoteId;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Note {
    private final NoteId id;
    private final String title;
    private final String content;
    private final LocalDateTime created;

    public Note(NoteId id, String title, String content, LocalDateTime created) {
        this.id = Objects.requireNonNull(id, "Note ID cannot be null");
        this.title = validateTitle(title);
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.created = Objects.requireNonNull(created, "Created timestamp cannot be null");
    }

    private String validateTitle(String title) {
        Objects.requireNonNull(title, "Title cannot be null");
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title cannot exceed 200 characters");
        }
        return title;
    }

    // Getters
    public NoteId getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreated() { return created; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

**Calculation.java**
```java
package com.example.mcp.domain.model;

import com.example.mcp.domain.valueobject.Operation;
import java.util.Objects;

public final class Calculation {
    private final Operation operation;
    private final double operandA;
    private final double operandB;
    private final double result;

    private Calculation(Operation operation, double a, double b, double result) {
        this.operation = Objects.requireNonNull(operation);
        this.operandA = a;
        this.operandB = b;
        this.result = result;
    }

    public static Calculation perform(Operation operation, double a, double b) {
        double result = switch (operation) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero is not allowed");
                }
                yield a / b;
            }
        };
        return new Calculation(operation, a, b, result);
    }

    // Getters
    public Operation getOperation() { return operation; }
    public double getOperandA() { return operandA; }
    public double getOperandB() { return operandB; }
    public double getResult() { return result; }

    public String format() {
        return String.format("Result: %.2f %s %.2f = %.2f",
            operandA, operation.getSymbol(), operandB, result);
    }
}
```

**Weather.java**
```java
package com.example.mcp.domain.model;

import com.example.mcp.domain.valueobject.*;
import java.util.Objects;

public final class Weather {
    private final CityName city;
    private final String countryName;
    private final Temperature temperature;
    private final String condition;
    private final WindSpeed windSpeed;

    public Weather(CityName city, String countryName, Temperature temperature,
                   String condition, WindSpeed windSpeed) {
        this.city = Objects.requireNonNull(city);
        this.countryName = Objects.requireNonNull(countryName);
        this.temperature = Objects.requireNonNull(temperature);
        this.condition = Objects.requireNonNull(condition);
        this.windSpeed = Objects.requireNonNull(windSpeed);
    }

    // Getters
    public CityName getCity() { return city; }
    public String getCountryName() { return countryName; }
    public Temperature getTemperature() { return temperature; }
    public String getCondition() { return condition; }
    public WindSpeed getWindSpeed() { return windSpeed; }

    public String format() {
        return String.format("Weather in %s (%s):\n🌡️ Temperature: %s\n☁️ Condition: %s\n💨 Wind: %s",
            city.getValue(), countryName, temperature, condition, windSpeed);
    }
}
```

**FileMetadata.java**
```java
package com.example.mcp.domain.model;

import com.example.mcp.domain.valueobject.FilePath;
import com.example.mcp.domain.valueobject.FileSize;
import java.util.Objects;

public final class FileMetadata {
    private final FilePath path;
    private final String name;
    private final FileSize size;
    private final boolean isDirectory;

    public FileMetadata(FilePath path, String name, FileSize size, boolean isDirectory) {
        this.path = Objects.requireNonNull(path);
        this.name = Objects.requireNonNull(name);
        this.size = Objects.requireNonNull(size);
        this.isDirectory = isDirectory;
    }

    // Getters
    public FilePath getPath() { return path; }
    public String getName() { return name; }
    public FileSize getSize() { return size; }
    public boolean isDirectory() { return isDirectory; }
}
```

#### 1.3 Value Objects

**Operation.java** (Enum)
```java
package com.example.mcp.domain.valueobject;

public enum Operation {
    ADD("add", "+"),
    SUBTRACT("subtract", "-"),
    MULTIPLY("multiply", "×"),
    DIVIDE("divide", "÷");

    private final String name;
    private final String symbol;

    Operation(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() { return name; }
    public String getSymbol() { return symbol; }

    public static Operation fromString(String name) {
        for (Operation op : values()) {
            if (op.name.equalsIgnoreCase(name)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operation: " + name);
    }
}
```

**NoteId.java, Temperature.java, WindSpeed.java, etc.** (Value object pattern with validation)

---

### Phase 2: Application Layer (Use Cases)

#### 2.1 Define Input Ports (Use Case Interfaces)

```
src/main/java/com/example/mcp/application/port/in/
├── CalculationUseCase.java
├── NoteManagementUseCase.java
├── WeatherQueryUseCase.java
├── FileOperationUseCase.java
├── ResourceQueryUseCase.java
└── PromptGenerationUseCase.java
```

**Example: NoteManagementUseCase.java**
```java
package com.example.mcp.application.port.in;

import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;
import java.util.List;
import java.util.Optional;

public interface NoteManagementUseCase {
    Note createNote(String title, String content);
    List<Note> listAllNotes();
    Optional<Note> getNoteById(NoteId id);
}
```

**Example: WeatherQueryUseCase.java**
```java
package com.example.mcp.application.port.in;

import com.example.mcp.domain.model.Weather;
import com.example.mcp.domain.valueobject.CityName;

public interface WeatherQueryUseCase {
    Weather getWeatherForCity(CityName city) throws WeatherServiceException;
}
```

#### 2.2 Define Output Ports (Repository/Service Interfaces)

```
src/main/java/com/example/mcp/application/port/out/
├── NoteRepository.java
├── WeatherServicePort.java
├── FileSystemPort.java
├── LoggingPort.java
└── TimeProvider.java
```

**Example: NoteRepository.java**
```java
package com.example.mcp.application.port.out;

import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;
import java.util.List;
import java.util.Optional;

public interface NoteRepository {
    void save(Note note);
    Optional<Note> findById(NoteId id);
    List<Note> findAll();
    NoteId nextIdentity();
}
```

**Example: WeatherServicePort.java**
```java
package com.example.mcp.application.port.out;

import com.example.mcp.domain.valueobject.CityName;
import com.example.mcp.domain.valueobject.Coordinates;
import com.example.mcp.domain.valueobject.Temperature;
import com.example.mcp.domain.valueobject.WindSpeed;

public interface WeatherServicePort {
    
    record GeocodeResult(Coordinates coordinates, String countryName) {}
    
    record WeatherData(Temperature temperature, int weatherCode, WindSpeed windSpeed) {}
    
    GeocodeResult geocode(CityName city) throws WeatherServiceException;
    
    WeatherData getCurrentWeather(Coordinates coordinates) throws WeatherServiceException;
}
```

#### 2.3 Implement Application Services

```
src/main/java/com/example/mcp/application/service/
├── CalculationService.java
├── NoteService.java
├── WeatherQueryService.java
├── FileService.java
├── ResourceService.java
└── PromptService.java
```

**Example: NoteService.java**
```java
package com.example.mcp.application.service;

import com.example.mcp.application.port.in.NoteManagementUseCase;
import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.application.port.out.TimeProvider;
import com.example.mcp.application.port.out.LoggingPort;
import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;
import java.util.List;
import java.util.Optional;

public final class NoteService implements NoteManagementUseCase {
    private final NoteRepository repository;
    private final TimeProvider timeProvider;
    private final LoggingPort logger;

    public NoteService(NoteRepository repository, TimeProvider timeProvider, LoggingPort logger) {
        this.repository = repository;
        this.timeProvider = timeProvider;
        this.logger = logger;
    }

    @Override
    public Note createNote(String title, String content) {
        NoteId id = repository.nextIdentity();
        Note note = new Note(id, title, content, timeProvider.now());
        repository.save(note);
        logger.info("Created note with ID: " + id);
        return note;
    }

    @Override
    public List<Note> listAllNotes() {
        return repository.findAll();
    }

    @Override
    public Optional<Note> getNoteById(NoteId id) {
        return repository.findById(id);
    }
}
```

---

### Phase 3: Adapters Layer

#### 3.1 Driven Adapters (Infrastructure)

```
src/main/java/com/example/mcp/adapter/out/
├── persistence/
│   └── InMemoryNoteRepository.java
├── weather/
│   └── OpenMeteoWeatherAdapter.java
├── filesystem/
│   └── JavaNioFileSystemAdapter.java
├── logging/
│   └── Slf4jLoggingAdapter.java
└── time/
    └── SystemTimeProvider.java
```

**Example: InMemoryNoteRepository.java**
```java
package com.example.mcp.adapter.out.persistence;

import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryNoteRepository implements NoteRepository {
    private final Map<NoteId, Note> storage = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public void save(Note note) {
        storage.put(note.getId(), note);
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Note> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public NoteId nextIdentity() {
        return new NoteId(idGenerator.getAndIncrement());
    }
}
```

#### 3.2 Driving Adapter (MCP Protocol)

```
src/main/java/com/example/mcp/adapter/in/mcp/
├── McpServer.java                    // Main entry point (replaces Main.java)
├── handler/
│   ├── McpToolHandler.java           // Handles tools/list, tools/call
│   ├── McpResourceHandler.java       // Handles resources/list, resources/read
│   └── McpPromptHandler.java         // Handles prompts/list, prompts/get
└── mapper/
    ├── ToolRequestMapper.java
    ├── ResourceResponseMapper.java
    └── PromptResponseMapper.java
```

**Example: McpServer.java**
```java
package com.example.mcp.adapter.in.mcp;

import com.example.mcp.adapter.in.mcp.handler.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class McpServer {
    private final Gson gson = new GsonBuilder().create();
    private final McpToolHandler toolHandler;
    private final McpResourceHandler resourceHandler;
    private final McpPromptHandler promptHandler;

    public McpServer(McpToolHandler toolHandler,
                     McpResourceHandler resourceHandler,
                     McpPromptHandler promptHandler) {
        this.toolHandler = toolHandler;
        this.resourceHandler = resourceHandler;
        this.promptHandler = promptHandler;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(System.out, true);

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonObject request = gson.fromJson(line, JsonObject.class);
                    JsonObject response = handleRequest(request);
                    writer.println(gson.toJson(response));
                } catch (JsonSyntaxException e) {
                    // Handle JSON parse errors
                }
            }
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private JsonObject handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : "";
        JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();

        if (!request.has("id")) {
            return new JsonObject(); // Notification
        }

        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", request.get("id"));

        try {
            switch (method) {
                case "initialize" -> response.add("result", initializeResult());
                case "tools/list" -> response.add("result", toolHandler.listTools());
                case "tools/call" -> response.add("result", toolHandler.callTool(params));
                case "resources/list" -> response.add("result", resourceHandler.listResources());
                case "resources/read" -> response.add("result", resourceHandler.readResource(params));
                case "prompts/list" -> response.add("result", promptHandler.listPrompts());
                case "prompts/get" -> response.add("result", promptHandler.getPrompt(params));
                default -> {
                    JsonObject error = new JsonObject();
                    error.addProperty("code", -32601);
                    error.addProperty("message", "Method not found: " + method);
                    response.add("error", error);
                }
            }
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32603);
            error.addProperty("message", e.getMessage());
            response.add("error", error);
        }

        return response;
    }

    private JsonObject initializeResult() {
        // Same as before
    }
}
```

---

### Phase 4: Configuration and Dependency Injection

```
src/main/java/com/example/mcp/config/
└── ApplicationConfiguration.java
```

**ApplicationConfiguration.java**
```java
package com.example.mcp.config;

import com.example.mcp.adapter.in.mcp.*;
import com.example.mcp.adapter.in.mcp.handler.*;
import com.example.mcp.adapter.out.persistence.InMemoryNoteRepository;
import com.example.mcp.adapter.out.weather.OpenMeteoWeatherAdapter;
import com.example.mcp.adapter.out.filesystem.JavaNioFileSystemAdapter;
import com.example.mcp.adapter.out.logging.Slf4jLoggingAdapter;
import com.example.mcp.adapter.out.time.SystemTimeProvider;
import com.example.mcp.application.port.out.*;
import com.example.mcp.application.service.*;

/**
 * Manual dependency injection configuration.
 * In a larger project, this could be replaced with Spring, Guice, or Dagger.
 */
public final class ApplicationConfiguration {

    public static McpServer createMcpServer() {
        // Output ports (infrastructure)
        NoteRepository noteRepository = new InMemoryNoteRepository();
        WeatherServicePort weatherService = new OpenMeteoWeatherAdapter();
        FileSystemPort fileSystem = new JavaNioFileSystemAdapter();
        LoggingPort logger = new Slf4jLoggingAdapter();
        TimeProvider timeProvider = new SystemTimeProvider();

        // Application services (use cases)
        CalculationService calculationService = new CalculationService(logger);
        NoteService noteService = new NoteService(noteRepository, timeProvider, logger);
        WeatherQueryService weatherService = new WeatherQueryService(weatherService, logger);
        FileService fileService = new FileService(fileSystem, logger);
        ResourceService resourceService = new ResourceService(noteRepository);
        PromptService promptService = new PromptService(noteRepository);

        // Input adapters (handlers)
        McpToolHandler toolHandler = new McpToolHandler(
            calculationService, noteService, weatherService, fileService);
        McpResourceHandler resourceHandler = new McpResourceHandler(resourceService);
        McpPromptHandler promptHandler = new McpPromptHandler(promptService);

        // MCP Server
        return new McpServer(toolHandler, resourceHandler, promptHandler);
    }

    public static void main(String[] args) {
        McpServer server = createMcpServer();
        server.run();
    }
}
```

---

### Phase 5: Testing Strategy

#### 5.1 Unit Tests (Domain Layer)
- Test domain entities with invalid inputs
- Test calculation logic
- Test value object validation

#### 5.2 Unit Tests (Application Layer)
- Test services with mocked repositories/ports
- Verify business logic without infrastructure

#### 5.3 Integration Tests (Adapters)
- Test each adapter independently
- Mock external dependencies (HTTP, file system)

#### 5.4 End-to-End Test
- Refactor `IntegrationJvmTest` to test complete flow
- Verify MCP protocol compliance

---

## Migration Strategy

### Step-by-Step Execution

1. **Create new package structure** (parallel to existing)
2. **Implement domain layer** (value objects, entities)
3. **Define ports** (interfaces only)
4. **Implement application services** (use case implementations)
5. **Implement driven adapters** (infrastructure)
6. **Implement driving adapter** (MCP protocol)
7. **Create configuration class**
8. **Update Main.java** to delegate to ApplicationConfiguration
9. **Run tests** (verify everything works)
10. **Remove old classes** (ToolsManager, ResourceManager, PromptManager)
11. **Update documentation**

### Compatibility Notes

- **Zero breaking changes** to MCP protocol behavior
- **Identical JSON-RPC responses**
- **Same stdio transport**
- **Same logging output**
- Internal refactoring only

---

## Benefits After Refactoring

### 1. Testability
- Unit test business logic without MCP protocol
- Mock external dependencies easily
- Test each layer in isolation

### 2. Maintainability
- Clear separation of concerns
- Single Responsibility Principle enforced
- Easy to locate and fix bugs

### 3. Flexibility
- Swap implementations (memory → database)
- Add new adapters (REST API, gRPC)
- Change weather providers without touching business logic

### 4. Domain-Driven Design
- Business logic in domain layer (language ubiquitous)
- Rich domain models with validation
- Value objects prevent invalid states

### 5. Future-Proof
- Easy to add new use cases
- Support multiple protocols simultaneously
- Migrate to frameworks (Spring) without rewriting logic

---

## Estimated File Count

### New Files: ~45-50
- Domain: ~15 files (entities, value objects, domain services)
- Application: ~15 files (ports, services)
- Adapters: ~12 files (driven + driving)
- Config: 1 file
- Tests: ~15-20 files

### Modified Files: 2
- Main.java (slim entry point)
- IntegrationJvmTest.java (updated)

### Deleted Files: 3
- ToolsManager.java
- ResourceManager.java
- PromptManager.java

### Final Structure: ~50 files total (vs. current 6 files)

---

## Risk Assessment

### Low Risk
- Pure addition of new code
- Old code remains functional during migration
- Can test incrementally

### Medium Risk
- Large refactoring (45+ files)
- Potential for copy-paste errors
- Need thorough testing

### Mitigation
- Implement in phases
- Test after each phase
- Keep git commits small and focused
- Run integration test frequently

---

## Timeline Estimate

- **Phase 1** (Domain): 2-3 hours
- **Phase 2** (Application): 2-3 hours
- **Phase 3** (Adapters): 3-4 hours
- **Phase 4** (Config): 30 minutes
- **Phase 5** (Testing): 2-3 hours
- **Documentation**: 1 hour

**Total: 10-14 hours** of focused development time

---

## Next Steps

1. **Review this plan** with stakeholders
2. **Approve architecture** decisions
3. **Begin Phase 1** (Domain layer implementation)
4. **Iterate through phases** with testing at each step
5. **Final verification** and documentation update

---

## Questions to Address

1. Do we want to use a DI framework (Spring, Guice) or manual injection?
2. Should we add database persistence option now or later?
3. Should we support multiple weather providers (adapter registry)?
4. Do we want to add validation annotations (JSR-303)?
5. Should we create interfaces for all domain services?

---

**Document Version**: 1.0  
**Created**: 2025-12-01  
**Author**: GitHub Copilot (via RolleRocker)  
**Status**: PENDING APPROVAL
