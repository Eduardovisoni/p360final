package com.example.presion360.data.model;

import com.google.gson.annotations.SerializedName;

public class MeasurementData {

    @SerializedName("email")
    private String email;

    @SerializedName("systolic")
    private int systolic;

    @SerializedName("diastolic")
    private int diastolic;

    @SerializedName("pulse")
    private int pulse;

    @SerializedName("status")
    private String status;

    @SerializedName("notes")
    private String notes;

    public MeasurementData(String email, int systolic, int diastolic, int pulse, String status, String notes) {
        this.email = email;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.status = status;
        this.notes = notes;
    }
}
