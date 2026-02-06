package com.mycompany.clipper.models;

public class ClipboardEntry {
    private int id;
    private String text;
    private byte[] imageData;
    private String timestamp;

    public ClipboardEntry(int id, String text, String timestamp) {
        this(id, text, null, timestamp);
    }

    public ClipboardEntry(int id, String text, byte[] imageData, String timestamp) {
        this.id = id;
        this.text = text;
        this.imageData = imageData;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }

    @Override
    public String toString() {
        return text != null ? text : "[Imagen]";
    }
}
