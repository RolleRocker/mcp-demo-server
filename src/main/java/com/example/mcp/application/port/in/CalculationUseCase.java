package com.example.mcp.application.port.in;

import com.example.mcp.domain.model.Calculation;
import com.example.mcp.domain.valueobject.Operation;

/**
 * Input port (use case interface) for performing calculations.
 * This defines what the application can do with calculations.
 */
public interface CalculationUseCase {
    
    /**
     * Performs a mathematical calculation.
     * 
     * @param operation The operation to perform (add, subtract, multiply, divide)
     * @param a First operand
     * @param b Second operand
     * @return The calculation result
     * @throws ArithmeticException if the operation is invalid (e.g., division by zero)
     */
    Calculation calculate(Operation operation, double a, double b);
}
