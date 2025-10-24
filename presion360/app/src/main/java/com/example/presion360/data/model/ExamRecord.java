package com.example.presion360.data.model;

public class ExamRecord {
    private long id;
    private String userEmail;
    private long timestamp;
    private int systolic;
    private int diastolic;
    private int pulse;
    private String notes;
    private String category; // Nuevo campo

    public ExamRecord(long id, String userEmail, long timestamp, int systolic, int diastolic, int pulse, String notes, String category) {
        this.id = id;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.notes = notes;
        this.category = category; // Asignar categoría
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSystolic() {
        return systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public int getPulse() {
        return pulse;
    }

    public String getNotes() {
        return notes;
    }

    public String getCategory() {
        return category; // Getter para categoría
    }
}
