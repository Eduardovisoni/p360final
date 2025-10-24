package com.example.presion360.data.model;

public class ChatSessionSummary {
    private long sessionId;
    private String previewText;
    private long startTimestamp;

    public ChatSessionSummary(long sessionId, String previewText, long startTimestamp) {
        this.sessionId = sessionId;
        this.previewText = previewText;
        this.startTimestamp = startTimestamp;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getPreviewText() {
        return previewText;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }
}
