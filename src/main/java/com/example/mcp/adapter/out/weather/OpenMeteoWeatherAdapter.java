package com.example.mcp.adapter.out.weather;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.example.mcp.application.port.out.WeatherServicePort;
import com.example.mcp.domain.valueobject.CityName;
import com.example.mcp.domain.valueobject.Coordinates;
import com.example.mcp.domain.valueobject.Temperature;
import com.example.mcp.domain.valueobject.WindSpeed;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Open-Meteo weather service adapter.
 * Implements weather service port using Open-Meteo public API.
 */
public final class OpenMeteoWeatherAdapter implements WeatherServicePort {
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public GeocodeResult geocode(CityName city) throws WeatherServiceException {
        try {
            String encodedCity = URLEncoder.encode(city.getValue(), StandardCharsets.UTF_8);
            String url = GEOCODING_API + "?name=" + encodedCity + "&count=1&language=en&format=json";
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(TIMEOUT)
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new WeatherServiceException("Geocoding service returned HTTP " + response.statusCode());
            }
            
            JsonObject data = gson.fromJson(response.body(), JsonObject.class);
            JsonElement resultsElem = data.get("results");
            
            if (resultsElem == null || !resultsElem.isJsonArray() || resultsElem.getAsJsonArray().size() == 0) {
                throw new WeatherServiceException("City not found: " + city.getValue());
            }
            
            JsonObject cityInfo = resultsElem.getAsJsonArray().get(0).getAsJsonObject();
            double latitude = cityInfo.get("latitude").getAsDouble();
            double longitude = cityInfo.get("longitude").getAsDouble();
            String countryName = cityInfo.has("country") ? cityInfo.get("country").getAsString() : "Unknown";
            
            Coordinates coordinates = new Coordinates(latitude, longitude);
            return new GeocodeResult(coordinates, countryName);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherServiceException("Geocoding request interrupted", e);
        } catch (IOException e) {
            throw new WeatherServiceException("Network error during geocoding", e);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to geocode city: " + city.getValue(), e);
        }
    }

    @Override
    public WeatherData getCurrentWeather(Coordinates coordinates) throws WeatherServiceException {
        try {
            String url = String.format(java.util.Locale.US,
                "%s?latitude=%.2f&longitude=%.2f&current=temperature_2m,weather_code,wind_speed_10m&temperature_unit=celsius",
                WEATHER_API, coordinates.getLatitude(), coordinates.getLongitude());
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(TIMEOUT)
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new WeatherServiceException("Weather service returned HTTP " + response.statusCode());
            }
            
            JsonObject data = gson.fromJson(response.body(), JsonObject.class);
            JsonObject current = data.getAsJsonObject("current");
            
            double temp = current.get("temperature_2m").getAsDouble();
            int weatherCode = current.get("weather_code").getAsInt();
            double windSpeedValue = current.get("wind_speed_10m").getAsDouble();
            
            return new WeatherData(
                Temperature.celsius(temp),
                weatherCode,
                WindSpeed.kmPerHour(windSpeedValue)
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherServiceException("Weather request interrupted", e);
        } catch (IOException e) {
            throw new WeatherServiceException("Network error fetching weather", e);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to fetch weather data", e);
        }
    }
}
