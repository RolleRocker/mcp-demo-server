package com.example.mcp.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.mcp.application.port.out.NoteRepository;
import com.example.mcp.domain.model.Note;
import com.example.mcp.domain.valueobject.NoteId;

/**
 * In-memory implementation of NoteRepository.
 * Thread-safe storage using ConcurrentHashMap.
 */
public final class InMemoryNoteRepository implements NoteRepository {
    private final Map<NoteId, Note> storage = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public void save(Note note) {
        storage.put(note.getId(), note);
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Note> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public NoteId nextIdentity() {
        return new NoteId(idGenerator.getAndIncrement());
    }
}
