package com.example.mcp.application.service;

import com.example.mcp.application.port.in.WeatherQueryUseCase;
import com.example.mcp.application.port.out.LoggingPort;
import com.example.mcp.application.port.out.WeatherServicePort;
import com.example.mcp.domain.model.Weather;
import com.example.mcp.domain.service.WeatherConditionInterpreter;
import com.example.mcp.domain.valueobject.CityName;
import com.example.mcp.domain.valueobject.Coordinates;

/**
 * Application service implementing weather query use case.
 * Contains pure business logic with no infrastructure dependencies.
 */
public final class WeatherQueryService implements WeatherQueryUseCase {
    private final WeatherServicePort weatherService;
    private final LoggingPort logger;

    public WeatherQueryService(WeatherServicePort weatherService, LoggingPort logger) {
        this.weatherService = weatherService;
        this.logger = logger;
    }

    @Override
    public Weather getWeatherForCity(CityName city) throws WeatherServiceException {
        logger.info("Fetching weather for city: " + city.getValue());
        
        try {
            // Step 1: Geocode city to coordinates
            WeatherServicePort.GeocodeResult geocode = weatherService.geocode(city);
            Coordinates coords = geocode.coordinates();
            String country = geocode.countryName();
            
            logger.info("City located at coordinates: " + coords);
            
            // Step 2: Fetch weather data
            WeatherServicePort.WeatherData weatherData = weatherService.getCurrentWeather(coords);
            
            // Step 3: Interpret weather code using domain service
            String condition = WeatherConditionInterpreter.interpret(weatherData.weatherCode());
            
            // Step 4: Create domain model
            Weather weather = new Weather(
                city,
                country,
                weatherData.temperature(),
                condition,
                weatherData.windSpeed()
            );
            
            logger.info("Successfully fetched weather for " + city.getValue());
            return weather;
            
        } catch (WeatherServicePort.WeatherServiceException e) {
            logger.error("Weather service error for " + city.getValue() + ": " + e.getMessage());
            throw new WeatherServiceException("Failed to fetch weather for " + city.getValue(), e);
        }
    }
}
