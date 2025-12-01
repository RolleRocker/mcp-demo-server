package com.example.mcp.domain.model;

import java.util.Objects;

import com.example.mcp.domain.valueobject.CityName;
import com.example.mcp.domain.valueobject.Temperature;
import com.example.mcp.domain.valueobject.WindSpeed;

/**
 * Domain entity representing weather information for a location.
 */
public final class Weather {
    private final CityName city;
    private final String countryName;
    private final Temperature temperature;
    private final String condition;
    private final WindSpeed windSpeed;

    public Weather(CityName city, String countryName, Temperature temperature,
                   String condition, WindSpeed windSpeed) {
        this.city = Objects.requireNonNull(city, "City cannot be null");
        this.countryName = Objects.requireNonNull(countryName, "Country name cannot be null");
        this.temperature = Objects.requireNonNull(temperature, "Temperature cannot be null");
        this.condition = Objects.requireNonNull(condition, "Condition cannot be null");
        this.windSpeed = Objects.requireNonNull(windSpeed, "Wind speed cannot be null");
    }

    public CityName getCity() {
        return city;
    }

    public String getCountryName() {
        return countryName;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public WindSpeed getWindSpeed() {
        return windSpeed;
    }

    public String format() {
        return String.format("Weather in %s (%s):\n🌡️ Temperature: %s\n☁️ Condition: %s\n💨 Wind: %s",
            city.getValue(), countryName, temperature, condition, windSpeed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Weather)) return false;
        Weather weather = (Weather) o;
        return Objects.equals(city, weather.city) &&
               Objects.equals(countryName, weather.countryName) &&
               Objects.equals(temperature, weather.temperature) &&
               Objects.equals(condition, weather.condition) &&
               Objects.equals(windSpeed, weather.windSpeed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, countryName, temperature, condition, windSpeed);
    }

    @Override
    public String toString() {
        return format();
    }
}
