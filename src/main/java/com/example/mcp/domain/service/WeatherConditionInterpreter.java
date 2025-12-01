package com.example.mcp.domain.service;

/**
 * Domain service for interpreting WMO weather codes into human-readable conditions.
 * This is domain logic that doesn't belong to any specific entity.
 */
public final class WeatherConditionInterpreter {

    private WeatherConditionInterpreter() {
        // Utility class
    }

    /**
     * Interprets WMO Weather codes into readable conditions.
     * 
     * @param wmoCode The World Meteorological Organization weather code
     * @return Human-readable weather condition
     */
    public static String interpret(int wmoCode) {
        return switch (wmoCode) {
            case 0 -> "Clear sky";
            case 1, 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }
}
