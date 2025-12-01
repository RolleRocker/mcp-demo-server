package com.example.mcp.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.example.mcp.application.port.in.PromptGenerationUseCase;
import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.domain.model.Note;

/**
 * Application service implementing prompt generation use case.
 * Provides MCP prompt templates with dynamic content.
 */
public final class PromptService implements PromptGenerationUseCase {
    private final NoteRepository noteRepository;

    public PromptService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public List<Prompt> listPrompts() {
        List<Prompt> prompts = new ArrayList<>();
        
        // Helpful assistant prompt
        prompts.add(new Prompt(
            "helpful_assistant",
            "A helpful and friendly assistant persona",
            List.of(new PromptArgument("task", "The task to help with", true))
        ));
        
        // Code reviewer prompt
        prompts.add(new Prompt(
            "code_reviewer",
            "Review code and provide constructive feedback",
            List.of(
                new PromptArgument("language", "Programming language", true),
                new PromptArgument("code", "Code to review", true)
            )
        ));
        
        // Summarize notes prompt
        prompts.add(new Prompt(
            "summarize_notes",
            "Summarize all notes in the system",
            List.of()
        ));
        
        return prompts;
    }

    @Override
    public List<PromptMessage> generatePrompt(String name, Map<String, String> arguments) {
        List<PromptMessage> messages = new ArrayList<>();
        
        switch (name) {
            case "helpful_assistant" -> {
                String task = arguments.getOrDefault("task", "general assistance");
                String text = String.format(
                    "You are a helpful, friendly, and knowledgeable assistant. " +
                    "Please help me with the following task:\n\n%s\n\n" +
                    "Provide clear, accurate, and actionable guidance.",
                    task
                );
                messages.add(new PromptMessage("user", text));
            }
            
            case "code_reviewer" -> {
                String language = arguments.getOrDefault("language", "unknown");
                String code = arguments.getOrDefault("code", "");
                String text = String.format(
                    "Please review the following %s code and provide constructive feedback:\n\n" +
                    "```%s\n%s\n```\n\n" +
                    "Consider:\n" +
                    "- Code quality and readability\n" +
                    "- Potential bugs or issues\n" +
                    "- Performance concerns\n" +
                    "- Best practices\n" +
                    "- Suggestions for improvement",
                    language, language, code
                );
                messages.add(new PromptMessage("user", text));
            }
            
            case "summarize_notes" -> {
                List<Note> notes = noteRepository.findAll();
                
                if (notes.isEmpty()) {
                    messages.add(new PromptMessage("user",
                        "There are no notes to summarize. Please create some notes first using the create_note tool."));
                } else {
                    notes.sort(Comparator.comparing(n -> n.getId().getValue()));
                    
                    StringBuilder text = new StringBuilder("Please provide a concise summary of the following notes:\n\n");
                    for (Note note : notes) {
                        text.append("**").append(note.getTitle()).append("** (ID: ").append(note.getId()).append(")\n");
                        text.append(note.getContent()).append("\n\n---\n\n");
                    }
                    
                    messages.add(new PromptMessage("user", text.toString()));
                }
            }
            
            default -> throw new IllegalArgumentException("Unknown prompt: " + name);
        }
        
        return messages;
    }
}
