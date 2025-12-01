package com.example.mcp.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.example.mcp.domain.valueobject.NoteId;

/**
 * Domain entity representing a note with rich domain logic.
 */
public final class Note {
    private final NoteId id;
    private final String title;
    private final String content;
    private final LocalDateTime created;

    public Note(NoteId id, String title, String content, LocalDateTime created) {
        this.id = Objects.requireNonNull(id, "Note ID cannot be null");
        this.title = validateTitle(title);
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.created = Objects.requireNonNull(created, "Created timestamp cannot be null");
    }

    private String validateTitle(String title) {
        Objects.requireNonNull(title, "Title cannot be null");
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (trimmed.length() > 200) {
            throw new IllegalArgumentException("Title cannot exceed 200 characters");
        }
        return trimmed;
    }

    public NoteId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Note{id=" + id + ", title='" + title + "'}";
    }
}
