package com.example.mcp.application.port.out;

import java.util.List;
import java.util.Optional;

import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;

/**
 * Output port (repository interface) for persisting and retrieving notes.
 * This defines what the application needs from note storage.
 */
public interface NoteRepository {
    
    /**
     * Saves a note to the repository.
     * 
     * @param note The note to save
     */
    void save(Note note);
    
    /**
     * Finds a note by its ID.
     * 
     * @param id The note ID
     * @return Optional containing the note if found, empty otherwise
     */
    Optional<Note> findById(NoteId id);
    
    /**
     * Retrieves all notes from the repository.
     * 
     * @return List of all notes
     */
    List<Note> findAll();
    
    /**
     * Generates the next unique note ID.
     * 
     * @return A new unique NoteId
     */
    NoteId nextIdentity();
}
