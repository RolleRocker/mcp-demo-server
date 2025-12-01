package com.example.mcp.application.port.in;

import com.example.mcp.domain.model.Weather;
import com.example.mcp.domain.valueobject.CityName;

/**
 * Input port (use case interface) for querying weather information.
 * This defines what the application can do with weather data.
 */
public interface WeatherQueryUseCase {
    
    /**
     * Retrieves current weather information for a city.
     * 
     * @param city The city to get weather for
     * @return Weather information
     * @throws WeatherServiceException if the city is not found or the service is unavailable
     */
    Weather getWeatherForCity(CityName city) throws WeatherServiceException;
    
    /**
     * Exception thrown when weather service encounters an error.
     */
    class WeatherServiceException extends Exception {
        public WeatherServiceException(String message) {
            super(message);
        }
        
        public WeatherServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
