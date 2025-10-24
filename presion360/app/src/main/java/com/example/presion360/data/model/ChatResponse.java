package com.example.presion360.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("response")
    private String response;

    public String getResponse() {
        return response;
    }
}
