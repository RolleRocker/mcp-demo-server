package com.example.mcp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PromptManager {
    private final Gson gson = new Gson();
    private final Map<Integer, Note> notes;

    public PromptManager(Map<Integer, Note> notes) {
        this.notes = notes;
    }

    public JsonObject listPrompts() {
        JsonObject result = new JsonObject();
        List<JsonObject> prompts = new ArrayList<>();

        JsonObject helpfulPrompt = new JsonObject(); helpfulPrompt.addProperty("name","helpful_assistant"); helpfulPrompt.addProperty("description","A helpful and friendly assistant persona"); List<JsonObject> helpfulArgs = new ArrayList<>(); JsonObject taskArg = new JsonObject(); taskArg.addProperty("name","task"); taskArg.addProperty("description","The task to help with"); taskArg.addProperty("required", true); helpfulArgs.add(taskArg); helpfulPrompt.add("arguments", gson.toJsonTree(helpfulArgs)); prompts.add(helpfulPrompt);

        JsonObject reviewPrompt = new JsonObject(); reviewPrompt.addProperty("name","code_reviewer"); reviewPrompt.addProperty("description","Review code and provide constructive feedback"); List<JsonObject> reviewArgs = new ArrayList<>(); JsonObject langArg = new JsonObject(); langArg.addProperty("name","language"); langArg.addProperty("description","Programming language"); langArg.addProperty("required", true); reviewArgs.add(langArg); JsonObject codeArg = new JsonObject(); codeArg.addProperty("name","code"); codeArg.addProperty("description","Code to review"); codeArg.addProperty("required", true); reviewArgs.add(codeArg); reviewPrompt.add("arguments", gson.toJsonTree(reviewArgs)); prompts.add(reviewPrompt);

        JsonObject summarizePrompt = new JsonObject(); summarizePrompt.addProperty("name","summarize_notes"); summarizePrompt.addProperty("description","Summarize all notes in the system"); summarizePrompt.add("arguments", gson.toJsonTree(new ArrayList<>())); prompts.add(summarizePrompt);

        result.add("prompts", gson.toJsonTree(prompts));
        return result;
    }

    public JsonObject getPrompt(JsonObject params) {
        String name = params.get("name").getAsString();
        JsonObject args = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();
        JsonObject result = new JsonObject();
        List<JsonObject> messages = new ArrayList<>();

        switch (name) {
            case "helpful_assistant" -> {
                String task = args.has("task") ? args.get("task").getAsString() : "general assistance";
                String helpfulText = String.format("You are a helpful, friendly, and knowledgeable assistant. Please help me with the following task:\n\n%s\n\nProvide clear, accurate, and actionable guidance.", task);
                messages.add(createMessage("user", helpfulText));
            }
            case "code_reviewer" -> {
                String language = args.has("language") ? args.get("language").getAsString() : "unknown";
                String code = args.has("code") ? args.get("code").getAsString() : "";
                String reviewText = String.format("Please review the following %s code and provide constructive feedback:\n\n```%s\n%s\n```\n\nConsider:\n- Code quality and readability\n- Potential bugs or issues\n- Performance concerns\n- Best practices\n- Suggestions for improvement", language, language, code);
                messages.add(createMessage("user", reviewText));
            }
            case "summarize_notes" -> {
                if (notes.isEmpty()) {
                    messages.add(createMessage("user", "There are no notes to summarize. Please create some notes first using the create_note tool."));
                } else {
                    StringBuilder notesText = new StringBuilder("Please provide a concise summary of the following notes:\n\n");
                    notes.values().stream().sorted(Comparator.comparingInt(Note::getId)).forEach(note -> {
                        notesText.append("**").append(note.getTitle()).append("** (ID: ").append(note.getId()).append(")\n");
                        notesText.append(note.getContent()).append("\n\n---\n\n");
                    });
                    messages.add(createMessage("user", notesText.toString()));
                }
            }
            default -> throw new IllegalArgumentException("Unknown prompt: " + name);
        }

        result.add("messages", gson.toJsonTree(messages));
        return result;
    }

    private JsonObject createMessage(String role, String text) {
        JsonObject message = new JsonObject(); message.addProperty("role", role); JsonObject content = new JsonObject(); content.addProperty("type","text"); content.addProperty("text", text); message.add("content", content); return message;
    }
}
