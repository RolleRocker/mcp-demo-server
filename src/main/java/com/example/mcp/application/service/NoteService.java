package com.example.mcp.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.example.mcp.application.port.in.NoteManagementUseCase;
import com.example.mcp.application.port.out.LoggingPort;
import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.application.port.out.TimeProvider;
import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;

/**
 * Application service implementing note management use case.
 * Contains pure business logic with no infrastructure dependencies.
 */
public final class NoteService implements NoteManagementUseCase {
    private final NoteRepository repository;
    private final TimeProvider timeProvider;
    private final LoggingPort logger;

    public NoteService(NoteRepository repository, TimeProvider timeProvider, LoggingPort logger) {
        this.repository = repository;
        this.timeProvider = timeProvider;
        this.logger = logger;
    }

    @Override
    public Note createNote(String title, String content) {
        logger.info("Creating note with title: " + title);
        
        NoteId id = repository.nextIdentity();
        Note note = new Note(id, title, content, timeProvider.now());
        repository.save(note);
        
        logger.info("Created note with ID: " + id);
        return note;
    }

    @Override
    public List<Note> listAllNotes() {
        logger.info("Listing all notes");
        
        List<Note> notes = repository.findAll();
        notes.sort(Comparator.comparing(note -> note.getId().getValue()));
        
        logger.info("Found " + notes.size() + " notes");
        return notes;
    }

    @Override
    public Optional<Note> getNoteById(NoteId id) {
        logger.info("Retrieving note with ID: " + id);
        return repository.findById(id);
    }
}
