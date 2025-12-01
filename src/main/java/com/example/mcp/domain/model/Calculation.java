package com.example.mcp.domain.model;

import java.util.Objects;

import com.example.mcp.domain.valueobject.Operation;

/**
 * Domain entity representing a mathematical calculation.
 */
public final class Calculation {
    private final Operation operation;
    private final double operandA;
    private final double operandB;
    private final double result;

    private Calculation(Operation operation, double a, double b, double result) {
        this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        this.operandA = a;
        this.operandB = b;
        this.result = result;
    }

    /**
     * Performs a calculation and returns the result.
     * 
     * @param operation The arithmetic operation to perform
     * @param a First operand
     * @param b Second operand
     * @return A new Calculation instance with the computed result
     * @throws ArithmeticException if division by zero is attempted
     */
    public static Calculation perform(Operation operation, double a, double b) {
        double result = switch (operation) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero is not allowed");
                }
                yield a / b;
            }
        };
        return new Calculation(operation, a, b, result);
    }

    public Operation getOperation() {
        return operation;
    }

    public double getOperandA() {
        return operandA;
    }

    public double getOperandB() {
        return operandB;
    }

    public double getResult() {
        return result;
    }

    public String format() {
        return String.format("Result: %.2f %s %.2f = %.2f",
            operandA, operation.getSymbol(), operandB, result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Calculation)) return false;
        Calculation that = (Calculation) o;
        return Double.compare(that.operandA, operandA) == 0 &&
               Double.compare(that.operandB, operandB) == 0 &&
               Double.compare(that.result, result) == 0 &&
               operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, operandA, operandB, result);
    }

    @Override
    public String toString() {
        return format();
    }
}
