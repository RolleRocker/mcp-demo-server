package com.example.mcp.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing wind speed in km/h.
 */
public final class WindSpeed {
    private final double kmPerHour;

    private WindSpeed(double kmPerHour) {
        this.kmPerHour = kmPerHour;
    }

    public static WindSpeed kmPerHour(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative: " + value);
        }
        return new WindSpeed(value);
    }

    public double getKmPerHour() {
        return kmPerHour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WindSpeed)) return false;
        WindSpeed windSpeed = (WindSpeed) o;
        return Double.compare(windSpeed.kmPerHour, kmPerHour) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kmPerHour);
    }

    @Override
    public String toString() {
        return String.format("%.1f km/h", kmPerHour);
    }
}
