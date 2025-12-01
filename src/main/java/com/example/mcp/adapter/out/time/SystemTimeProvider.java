package com.example.mcp.adapter.out.time;

import java.time.LocalDateTime;

import com.example.mcp.application.port.out.TimeProvider;

/**
 * System time provider adapter.
 * Implements time provider port using system clock.
 */
public final class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
