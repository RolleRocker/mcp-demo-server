package com.example.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class SimpleLogger {
    private static final Logger logger = LoggerFactory.getLogger("mcp-demo-server");

    private SimpleLogger() {}

    public static void log(String msg) {
        try {
            String timestamp = java.time.ZonedDateTime.now().toString();
            String entry = timestamp + " " + msg;

            // Log via SLF4J (configured to write to stderr by simplelogger.properties)
            logger.info(msg);

            // Also persist to file for historical reasons
            try {
                Path logPath = Paths.get("mcp-demo-server.log");
                Files.write(logPath, (entry + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ioe) {
                logger.error("Failed to write to mcp-demo-server.log: {}", ioe.getMessage());
            }
        } catch (Exception ex) {
            // As a last resort, print to stderr
            System.err.println("[LOG ERROR] " + ex.getMessage());
        }
    }
}
