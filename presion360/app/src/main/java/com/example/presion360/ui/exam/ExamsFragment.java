package com.example.presion360.ui.exam;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.presion360.R;
import com.example.presion360.data.api.ApiClient;
import com.example.presion360.data.api.N8nApiService;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.data.model.MeasurementData;
import com.example.presion360.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamsFragment extends Fragment {

    private static final String TAG = "ExamsFragment";

    private EditText editTextSystolic;
    private EditText editTextDiastolic;
    private EditText editTextPulse;
    private EditText editTextExamNotes;
    private Button buttonCalculatePressure;
    private TextView textViewPressureResult;
    private TextView textViewSystolicError;
    private TextView textViewDiastolicError;
    private TextView textViewPulseError;
    private TextView textViewMedicalDisclaimer;
    private TextView textViewUrgentDisclaimer;

    private DatabaseHelper dbHelper;
    private String currentUserEmail;

    private static final int MIN_SYSTOLIC = 50;
    private static final int MAX_SYSTOLIC = 300;
    private static final int MIN_DIASTOLIC = 30;
    private static final int MAX_DIASTOLIC = 200;
    private static final int MIN_PULSE = 30;
    private static final int MAX_PULSE = 250;

    public ExamsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dbHelper = new DatabaseHelper(context);
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            currentUserEmail = SessionManager.getInstance(getActivity().getApplicationContext()).getCurrentUserEmail();
            Log.d(TAG, "currentUserEmail obtenido en onAttach: " + currentUserEmail);
        } else {
            Log.e(TAG, "Error al obtener currentUserEmail en onAttach");
            currentUserEmail = ""; // Fallback
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exams, container, false);

        editTextSystolic = view.findViewById(R.id.editTextSystolic);
        editTextDiastolic = view.findViewById(R.id.editTextDiastolic);
        editTextPulse = view.findViewById(R.id.editTextPulse);
        editTextExamNotes = view.findViewById(R.id.editTextExamNotes);
        buttonCalculatePressure = view.findViewById(R.id.buttonCalculatePressure);
        textViewPressureResult = view.findViewById(R.id.textViewPressureResult);
        textViewSystolicError = view.findViewById(R.id.textViewSystolicError);
        textViewDiastolicError = view.findViewById(R.id.textViewDiastolicError);
        textViewPulseError = view.findViewById(R.id.textViewPulseError);
        textViewMedicalDisclaimer = view.findViewById(R.id.textViewMedicalDisclaimer);
        textViewUrgentDisclaimer = view.findViewById(R.id.textViewUrgentDisclaimer);

        buttonCalculatePressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideErrorMessagesAndDisclaimers();
                validateAndSaveExamData();
            }
        });

        return view;
    }

    private void hideErrorMessagesAndDisclaimers() {
        textViewSystolicError.setVisibility(View.GONE);
        textViewSystolicError.setText("");
        textViewDiastolicError.setVisibility(View.GONE);
        textViewDiastolicError.setText("");
        if (textViewPulseError != null) {
            textViewPulseError.setVisibility(View.GONE);
            textViewPulseError.setText("");
        }
        textViewPressureResult.setText("");
        textViewMedicalDisclaimer.setVisibility(View.GONE);
        textViewUrgentDisclaimer.setVisibility(View.GONE);
    }

    private void showSystolicError(String message) {
        textViewSystolicError.setText(message);
        textViewSystolicError.setVisibility(View.VISIBLE);
        textViewPressureResult.setText("");
    }

    private void showDiastolicError(String message) {
        textViewDiastolicError.setText(message);
        textViewDiastolicError.setVisibility(View.VISIBLE);
        textViewPressureResult.setText("");
    }

    private void showPulseError(String message) {
        if (textViewPulseError != null) {
            textViewPulseError.setText(message);
            textViewPulseError.setVisibility(View.VISIBLE);
        }
        textViewPressureResult.setText("");
    }


    private void validateAndSaveExamData() {
        String systolicStr = editTextSystolic.getText().toString().trim();
        String diastolicStr = editTextDiastolic.getText().toString().trim();
        String pulseStr = "";
        if (editTextPulse != null) {
            pulseStr = editTextPulse.getText().toString().trim();
        }
        String notes = "";
        if (editTextExamNotes != null) {
            notes = editTextExamNotes.getText().toString().trim();
        }

        boolean hasError = false;
        if (TextUtils.isEmpty(systolicStr)) {
            showSystolicError("Ingrese un valor para la presión sistólica.");
            hasError = true;
        }
        if (TextUtils.isEmpty(diastolicStr)) {
            showDiastolicError("Ingrese un valor para la presión diastólica.");
            hasError = true;
        }
        if (editTextPulse != null && TextUtils.isEmpty(pulseStr)) {
            showPulseError("Ingrese un valor para el pulso.");
            hasError = true;
        }

        if (hasError && (TextUtils.isEmpty(systolicStr) || TextUtils.isEmpty(diastolicStr) || (editTextPulse != null && TextUtils.isEmpty(pulseStr)))) {
            return;
        }

        int systolic = 0;
        int diastolic = 0;
        int pulse = 0;

        try {
            systolic = Integer.parseInt(systolicStr);
        } catch (NumberFormatException e) {
            if (!TextUtils.isEmpty(systolicStr)) {
                showSystolicError("Ingrese un número válido para la sistólica.");
                hasError = true;
            }
        }

        try {
            diastolic = Integer.parseInt(diastolicStr);
        } catch (NumberFormatException e) {
            if (!TextUtils.isEmpty(diastolicStr)) {
                showDiastolicError("Ingrese un número válido para la diastólica.");
                hasError = true;
            }
        }

        if (editTextPulse != null && !TextUtils.isEmpty(pulseStr)) {
            try {
                pulse = Integer.parseInt(pulseStr);
            } catch (NumberFormatException e) {
                showPulseError("Ingrese un número válido para el pulso.");
                hasError = true;
            }
        } else if (editTextPulse != null && TextUtils.isEmpty(pulseStr)) {
            // Error ya manejado
        } else {
            pulse = 0; 
        }

        if (systolic != 0 && (systolic < MIN_SYSTOLIC || systolic > MAX_SYSTOLIC)) {
            showSystolicError("Sistólica: ingrese un valor entre " + MIN_SYSTOLIC + " y " + MAX_SYSTOLIC + ".");
            hasError = true;
        }

        if (diastolic != 0 && (diastolic < MIN_DIASTOLIC || diastolic > MAX_DIASTOLIC)) {
            showDiastolicError("Diastólica: ingrese un valor entre " + MIN_DIASTOLIC + " y " + MAX_DIASTOLIC + ".");
            hasError = true;
        }

        if (editTextPulse != null && pulse != 0 && (pulse < MIN_PULSE || pulse > MAX_PULSE)) {
            showPulseError("Pulso: ingrese un valor entre " + MIN_PULSE + " y " + MAX_PULSE + ".");
            hasError = true;
        }

        if (systolic != 0 && diastolic != 0 && systolic <= diastolic) {
            showSystolicError("La presión sistólica debe ser mayor que la diastólica.");
            showDiastolicError("La presión diastólica debe ser menor que la sistólica.");
            hasError = true;
        }

        if (hasError) return;

        String category;
        int color;
        boolean isNormal = false;

        if (systolic < 90 && diastolic < 60) {
            category = "Presión Baja (Hipotensión)";
            color = Color.BLUE;
        } else if (systolic < 120 && diastolic < 80) {
            category = "Normal";
            color = Color.parseColor("#4CAF50");
            isNormal = true;
        } else if (systolic >= 120 && systolic <= 129 && diastolic < 80) {
            category = "Elevada";
            color = Color.parseColor("#FFEB3B");
        } else if ((systolic >= 130 && systolic <= 139) || (diastolic >= 80 && diastolic <= 89)) {
            category = "Hipertensión Nivel 1";
            color = Color.parseColor("#FF9800");
        } else if (systolic >= 140 || diastolic >= 90) {
            if (systolic > 180 || diastolic > 120) {
                category = "Crisis Hipertensiva ¡Consulte a su médico urgentemente!";
                color = Color.parseColor("#B71C1C");
            } else {
                category = "Hipertensión Nivel 2";
                color = Color.parseColor("#F44336");
            }
        } else {
            category = "Valores no categorizados. Verifique.";
            color = Color.GRAY;
        }

        textViewPressureResult.setText("Categoría: " + category);
        textViewPressureResult.setTextColor(color);
        textViewMedicalDisclaimer.setVisibility(View.VISIBLE);

        if (!isNormal) {
            textViewUrgentDisclaimer.setVisibility(View.VISIBLE);
        } else {
            textViewUrgentDisclaimer.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(currentUserEmail)) {
            Log.e(TAG, "Email de usuario vacío. No se puede guardar el examen.");
            Toast.makeText(getContext(), "Error: No se pudo identificar al usuario. Intente iniciar sesión de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }

        long currentTime = System.currentTimeMillis();
        // Pasar la categoría al método addExamRecord
        boolean success = dbHelper.addExamRecord(currentUserEmail, currentTime, systolic, diastolic, pulse, notes, category);

        if (success) {
            Toast.makeText(getContext(), "Examen guardado correctamente.", Toast.LENGTH_SHORT).show();
            
            // --- INICIO DE LA NUEVA FUNCIONALIDAD ---
            // Después de guardar localmente, enviar a Google Sheets a través de n8n
            sendMeasurementToN8n(currentUserEmail, systolic, diastolic, pulse, category, notes);
            // --- FIN DE LA NUEVA FUNCIONALIDAD ---

        } else {
            Toast.makeText(getContext(), "Error al guardar el examen.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al guardar el examen para el usuario: " + currentUserEmail);
            textViewMedicalDisclaimer.setVisibility(View.GONE);
            textViewUrgentDisclaimer.setVisibility(View.GONE);
        }
    }

    // --- INICIO DEL NUEVO MÉTODO ---
    private void sendMeasurementToN8n(String email, int systolic, int diastolic, int pulse, String status, String notes) {
        // Asegurarse de que el contexto no es nulo
        if (getContext() == null) {
            Log.e(TAG, "Contexto nulo, no se puede inicializar ApiClient.");
            return;
        }
        
        N8nApiService apiService = ApiClient.getClient().create(N8nApiService.class);
        MeasurementData measurementData = new MeasurementData(email, systolic, diastolic, pulse, status, notes);

        Call<Void> call = apiService.saveMeasurementToSheet(measurementData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Medición enviada a n8n (Google Sheets) correctamente.");
                    // Opcional: Mostrar un Toast de sincronización exitosa
                    // Toast.makeText(getContext(), "Datos sincronizados.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error al enviar medición a n8n. Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo en la conexión al enviar medición a n8n: " + t.getMessage());
            }
        });
    }
    // --- FIN DEL NUEVO MÉTODO ---
}
