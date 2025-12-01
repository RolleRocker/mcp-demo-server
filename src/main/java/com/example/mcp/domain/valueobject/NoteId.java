package com.example.mcp.domain.valueobject;

import java.util.Objects;

/**
 * Value object representing a unique note identifier.
 */
public final class NoteId {
    private final int value;

    public NoteId(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Note ID must be positive, got: " + value);
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteId)) return false;
        NoteId noteId = (NoteId) o;
        return value == noteId.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
