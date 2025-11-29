package com.example.mcp;

import java.time.LocalDateTime;

public class Note {
    private final int id;
    private final String title;
    private final String content;
    private final LocalDateTime created;

    public Note(int id, String title, String content, LocalDateTime created) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.created = created;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreated() { return created; }
}
