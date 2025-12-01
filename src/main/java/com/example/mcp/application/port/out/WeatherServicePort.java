package com.example.mcp.application.port.out;

import com.example.mcp.domain.valueobject.CityName;
import com.example.mcp.domain.valueobject.Coordinates;
import com.example.mcp.domain.valueobject.Temperature;
import com.example.mcp.domain.valueobject.WindSpeed;

/**
 * Output port (service interface) for fetching weather data.
 * This defines what the application needs from a weather service.
 */
public interface WeatherServicePort {
    
    /**
     * Result of a geocoding query.
     */
    record GeocodeResult(
        Coordinates coordinates,
        String countryName
    ) {}
    
    /**
     * Current weather data.
     */
    record WeatherData(
        Temperature temperature,
        int weatherCode,
        WindSpeed windSpeed
    ) {}
    
    /**
     * Converts a city name to geographical coordinates.
     * 
     * @param city The city name
     * @return Geocoding result with coordinates and country
     * @throws WeatherServiceException if the city cannot be found
     */
    GeocodeResult geocode(CityName city) throws WeatherServiceException;
    
    /**
     * Fetches current weather data for the given coordinates.
     * 
     * @param coordinates The geographical coordinates
     * @return Current weather data
     * @throws WeatherServiceException if weather data cannot be fetched
     */
    WeatherData getCurrentWeather(Coordinates coordinates) throws WeatherServiceException;
    
    /**
     * Exception thrown when the weather service encounters an error.
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
