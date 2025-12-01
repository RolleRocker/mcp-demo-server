package com.example.mcp.application.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port (use case interface) for generating MCP prompts.
 * This defines what the application can do with prompts.
 */
public interface PromptGenerationUseCase {
    
    /**
     * Represents a prompt template.
     */
    record Prompt(
        String name,
        String description,
        List<PromptArgument> arguments
    ) {}
    
    /**
     * Represents a prompt argument definition.
     */
    record PromptArgument(
        String name,
        String description,
        boolean required
    ) {}
    
    /**
     * Represents a generated prompt message.
     */
    record PromptMessage(
        String role,
        String text
    ) {}
    
    /**
     * Lists all available prompt templates.
     * 
     * @return List of available prompts
     */
    List<Prompt> listPrompts();
    
    /**
     * Generates a prompt with the given arguments.
     * 
     * @param name The prompt name
     * @param arguments The prompt arguments
     * @return List of prompt messages
     * @throws IllegalArgumentException if the prompt is not found or arguments are invalid
     */
    List<PromptMessage> generatePrompt(String name, Map<String, String> arguments);
}
