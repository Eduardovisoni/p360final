package com.example.presion360.data.model;

public class ChatMessage {
    private String message;
    private boolean isUserMessage;
    private long timestamp;

    // Constructor para mensajes nuevos (timestamp se genera automáticamente)
    public ChatMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor para mensajes recuperados de la base de datos (timestamp se provee)
    public ChatMessage(String message, boolean isUserMessage, long timestamp) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
