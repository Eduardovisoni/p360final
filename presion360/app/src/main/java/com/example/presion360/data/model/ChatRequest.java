package com.example.presion360.data.model;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;

@Keep
public class ChatRequest {

    @SerializedName("message")
    public String message;

    @SerializedName("user")
    public String user;

    // No-argument constructor for Gson
    public ChatRequest() {}
}
