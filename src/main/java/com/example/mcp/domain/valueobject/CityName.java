package com.example.mcp.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a city name.
 */
public final class CityName {
    private final String value;

    public CityName(String value) {
        Objects.requireNonNull(value, "City name cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("City name cannot exceed 100 characters");
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CityName)) return false;
        CityName cityName = (CityName) o;
        return value.equals(cityName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
