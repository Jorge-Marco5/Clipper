package com.mycompany.clipper.models;

public class ClipboardEntry {
    private int id;
    private String text;
    private String timestamp;

    public ClipboardEntry(int id, String text, String timestamp) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return text;
    }
}
