package com.example.mcp.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a temperature in Celsius.
 */
public final class Temperature {
    private final double celsius;

    private Temperature(double celsius) {
        this.celsius = celsius;
    }

    public static Temperature celsius(double value) {
        if (value < -273.15) {
            throw new IllegalArgumentException("Temperature cannot be below absolute zero: " + value);
        }
        return new Temperature(value);
    }

    public double getCelsius() {
        return celsius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Temperature)) return false;
        Temperature that = (Temperature) o;
        return Double.compare(that.celsius, celsius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(celsius);
    }

    @Override
    public String toString() {
        return String.format("%.1f°C", celsius);
    }
}
