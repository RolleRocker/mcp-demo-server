package com.example.mcp.application.service;

import com.example.mcp.application.port.in.CalculationUseCase;
import com.example.mcp.application.port.out.LoggingPort;
import com.example.mcp.domain.model.Calculation;
import com.example.mcp.domain.valueobject.Operation;

/**
 * Application service implementing calculation use case.
 * Contains pure business logic with no infrastructure dependencies.
 */
public final class CalculationService implements CalculationUseCase {
    private final LoggingPort logger;

    public CalculationService(LoggingPort logger) {
        this.logger = logger;
    }

    @Override
    public Calculation calculate(Operation operation, double a, double b) {
        logger.info("Performing calculation: " + a + " " + operation.getName() + " " + b);
        
        try {
            Calculation result = Calculation.perform(operation, a, b);
            logger.info("Calculation result: " + result.getResult());
            return result;
        } catch (ArithmeticException e) {
            logger.error("Calculation error: " + e.getMessage());
            throw e;
        }
    }
}
