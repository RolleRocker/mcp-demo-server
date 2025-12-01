package com.example.mcp.domain.valueobject;

/**
 * Value object representing an arithmetic operation.
 */
public enum Operation {
    ADD("add", "+"),
    SUBTRACT("subtract", "-"),
    MULTIPLY("multiply", "×"),
    DIVIDE("divide", "÷");

    private final String name;
    private final String symbol;

    Operation(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Operation fromString(String name) {
        for (Operation op : values()) {
            if (op.name.equalsIgnoreCase(name)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operation: " + name);
    }
}
