package com.example.mcp.application.port.out;

/**
 * Output port (service interface) for logging.
 * This defines what the application needs from a logging system.
 */
public interface LoggingPort {
    
    /**
     * Logs an informational message.
     * 
     * @param message The message to log
     */
    void info(String message);
    
    /**
     * Logs a warning message.
     * 
     * @param message The message to log
     */
    void warn(String message);
    
    /**
     * Logs an error message.
     * 
     * @param message The message to log
     */
    void error(String message);
    
    /**
     * Logs an error message with an exception.
     * 
     * @param message The message to log
     * @param throwable The exception
     */
    void error(String message, Throwable throwable);
}
