package com.example.presion360.ui.history;

import android.content.Context;
import android.content.SharedPreferences;
// import android.graphics.Color; // Ya no es necesario para el fondo
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presion360.R;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.data.model.ExamRecord;
import com.example.presion360.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ExamsHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ExamsHistoryActivity";

    private RecyclerView recyclerViewExamsHistory;
    private TextView textViewNoExamsHistory;
    private ExamHistoryAdapter examsHistoryAdapter;
    private DatabaseHelper dbHelper;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate INICIADO");
        setContentView(R.layout.activity_exams_history);

        dbHelper = new DatabaseHelper(this);
        if (getApplicationContext() != null) {
            this.currentUserEmail = SessionManager.getInstance(getApplicationContext()).getCurrentUserEmail();
            Log.d(TAG, "onCreate - currentUserEmail recuperado via SessionManager: '" + this.currentUserEmail + "'");
        } else {
            Log.e(TAG, "onCreate - getApplicationContext() es null. No se pudo inicializar SessionManager.");
            this.currentUserEmail = "";
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_exams_history));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewExamsHistory = findViewById(R.id.recyclerViewExamsHistory);
        textViewNoExamsHistory = findViewById(R.id.textViewNoExamsHistory);

        recyclerViewExamsHistory.setLayoutManager(new LinearLayoutManager(this));
        examsHistoryAdapter = new ExamHistoryAdapter(new ArrayList<>(), this);
        recyclerViewExamsHistory.setAdapter(examsHistoryAdapter);

        Log.d(TAG, "onCreate FINALIZADO.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume INICIADO. Verificando historial para email: '" + this.currentUserEmail + "'");

        if (TextUtils.isEmpty(this.currentUserEmail)) {
            if (getApplicationContext() != null) {
                 this.currentUserEmail = SessionManager.getInstance(getApplicationContext()).getCurrentUserEmail();
                 Log.w(TAG, "onResume - currentUserEmail estaba vacío, re-obtenido a: '" + this.currentUserEmail + "'");
            }
        }

        if (TextUtils.isEmpty(this.currentUserEmail)) {
            Log.e(TAG, "onResume - currentUserEmail SIGUE VACÍO. Mostrando error.");
            Toast.makeText(this, "Error al cargar historial: Usuario no identificado.", Toast.LENGTH_LONG).show();
            textViewNoExamsHistory.setText("Error: Usuario no identificado.");
            textViewNoExamsHistory.setVisibility(View.VISIBLE);
            recyclerViewExamsHistory.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "onResume - currentUserEmail es '" + this.currentUserEmail + "'. Procediendo a cargar historial.");
            loadExamsHistory();
            Log.d(TAG, "onResume - LLAMADA A loadExamsHistory() COMPLETADA.");
        }

        // Logs de diagnóstico de visibilidad (sin colores de fondo)
        Log.d(TAG, "onResume - Estado final: RecyclerView Visibility: " + viewVisibilityToString(recyclerViewExamsHistory) + ", TextViewNoHistory Visibility: " + viewVisibilityToString(textViewNoExamsHistory));
        if (recyclerViewExamsHistory.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "onResume - DIAGNÓSTICO: RecyclerView DEBERÍA SER VISIBLE (según la lógica).");
        }
        if (textViewNoExamsHistory.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "onResume - DIAGNÓSTICO: TextViewNoHistory DEBERÍA SER VISIBLE (según la lógica).");
        }
        if (recyclerViewExamsHistory.getVisibility() == View.GONE && textViewNoExamsHistory.getVisibility() == View.GONE){
            Log.e(TAG, "onResume - DIAGNÓSTICO: AMBAS VISTAS ESTÁN GONE. Esto es inesperado si hay o no hay datos.");
            textViewNoExamsHistory.setText("DIAGNÓSTICO: Ambas vistas GONE"); // Mensaje para un estado muy inesperado
            textViewNoExamsHistory.setVisibility(View.VISIBLE);
        }
         Log.d(TAG, "onResume FINALIZADO.");
    }

    private void loadExamsHistory() {
        Log.d(TAG, "loadExamsHistory INICIADO. Email para consulta: [" + this.currentUserEmail + "]");

        if (dbHelper == null || TextUtils.isEmpty(this.currentUserEmail)) {
            Log.e(TAG, "loadExamsHistory - dbHelper nulo o currentUserEmail vacío. No se puede cargar.");
            if (examsHistoryAdapter != null) examsHistoryAdapter.updateData(new ArrayList<>());
            textViewNoExamsHistory.setText(TextUtils.isEmpty(this.currentUserEmail) ? "Error: Usuario no identificado." : "Error interno al cargar historial.");
            textViewNoExamsHistory.setVisibility(View.VISIBLE);
            recyclerViewExamsHistory.setVisibility(View.GONE);
            Log.d(TAG, "loadExamsHistory - TERMINADO (error email/dbHelper)");
            return;
        }

        List<ExamRecord> records = dbHelper.getExamRecordsForUser(this.currentUserEmail);

        if (records != null && !records.isEmpty()) {
            Log.d(TAG, "Registros de exámenes encontrados: " + records.size() + ". Actualizando RecyclerView.");
            if (examsHistoryAdapter != null) examsHistoryAdapter.updateData(records);
            recyclerViewExamsHistory.setVisibility(View.VISIBLE);
            textViewNoExamsHistory.setVisibility(View.GONE);
        } else {
            Log.d(TAG, (records == null ? "DBHelper devolvió NULL." : "DBHelper devolvió lista VACÍA.") + " Mostrando mensaje 'no hay historial'.");
            if (examsHistoryAdapter != null) examsHistoryAdapter.updateData(new ArrayList<>());
            recyclerViewExamsHistory.setVisibility(View.GONE);
            textViewNoExamsHistory.setText(getString(R.string.no_exams_history_available));
            textViewNoExamsHistory.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "loadExamsHistory - TERMINADO.");
    }

    private String viewVisibilityToString(View view) {
        if (view == null) return "null";
        switch (view.getVisibility()) {
            case View.VISIBLE: return "VISIBLE";
            case View.INVISIBLE: return "INVISIBLE";
            case View.GONE: return "GONE";
            default: return "UNKNOWN";
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item); // Corregido para llamar a super
    }
}
