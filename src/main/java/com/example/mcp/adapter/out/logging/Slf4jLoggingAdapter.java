package com.example.mcp.adapter.out.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.mcp.application.port.out.LoggingPort;

/**
 * SLF4J logging adapter.
 * Implements logging port using SLF4J with file persistence.
 */
public final class Slf4jLoggingAdapter implements LoggingPort {
    private static final Logger logger = LoggerFactory.getLogger("mcp-demo-server");
    private static final Path LOG_FILE = Paths.get("mcp-demo-server.log");

    @Override
    public void info(String message) {
        logger.info(message);
        persistToFile(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
        persistToFile(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
        persistToFile(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
        persistToFile(message + " - " + throwable.getMessage());
    }

    private void persistToFile(String message) {
        try {
            String timestamp = ZonedDateTime.now().toString();
            String entry = timestamp + " " + message + System.lineSeparator();
            
            Files.write(LOG_FILE,
                entry.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Fallback to stderr if file logging fails
            System.err.println("[LOG ERROR] Failed to write to log file: " + e.getMessage());
        }
    }
}
