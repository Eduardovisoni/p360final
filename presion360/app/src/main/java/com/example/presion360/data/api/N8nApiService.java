package com.example.presion360.data.api;

import com.example.presion360.data.model.ChatRequest;
import com.example.presion360.data.model.ChatResponse;
import com.example.presion360.data.model.MeasurementData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface N8nApiService {
    // Webhook para el chatbot de IA (ruta relativa)
    @POST("webhook/9953d769-2e90-4065-b966-2838cd1b9755")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);

    // Webhook para guardar la medición en Google Sheets (ruta relativa)
    @POST("webhook/d152bd55-5d61-4dba-91a0-4468d6139f9e")
    Call<Void> saveMeasurementToSheet(@Body MeasurementData measurementData);
}
