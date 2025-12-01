package com.example.mcp.application.port.out;

import java.time.LocalDateTime;

/**
 * Output port (service interface) for getting the current time.
 * This allows time to be mocked in tests.
 */
public interface TimeProvider {
    
    /**
     * Returns the current date and time.
     * 
     * @return Current LocalDateTime
     */
    LocalDateTime now();
}
