package com.example.mcp.application.port.in;

import java.util.List;
import java.util.Optional;

import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;

/**
 * Input port (use case interface) for managing notes.
 * This defines what the application can do with notes.
 */
public interface NoteManagementUseCase {
    
    /**
     * Creates a new note with the given title and content.
     * 
     * @param title The note title
     * @param content The note content
     * @return The created note with assigned ID
     * @throws IllegalArgumentException if title or content is invalid
     */
    Note createNote(String title, String content);
    
    /**
     * Lists all notes in the system.
     * 
     * @return List of all notes, sorted by ID
     */
    List<Note> listAllNotes();
    
    /**
     * Retrieves a specific note by its ID.
     * 
     * @param id The note ID
     * @return Optional containing the note if found, empty otherwise
     */
    Optional<Note> getNoteById(NoteId id);
}
