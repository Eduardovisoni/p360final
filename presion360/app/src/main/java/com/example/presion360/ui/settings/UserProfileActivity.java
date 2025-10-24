package com.example.presion360.ui.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.utils.SessionManager;

public class UserProfileActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUserEmail;

    private TextView textViewNombreCompleto, textViewEdad, textViewSexo, textViewPeso, textViewAltura, textViewCondicionMedica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbHelper = new DatabaseHelper(this);
        currentUserEmail = SessionManager.getInstance(this).getCurrentUserEmail();

        textViewNombreCompleto = findViewById(R.id.textViewNombreCompleto);
        textViewEdad = findViewById(R.id.textViewEdad);
        textViewSexo = findViewById(R.id.textViewSexo);
        textViewPeso = findViewById(R.id.textViewPeso);
        textViewAltura = findViewById(R.id.textViewAltura);
        textViewCondicionMedica = findViewById(R.id.textViewCondicionMedica);

        loadUserProfile();
    }

    private void loadUserProfile() {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar al usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        Cursor cursor = dbHelper.getUserProfileByEmail(currentUserEmail);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                String nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_NOMBRE_COMPLETO));
                int edad = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_EDAD));
                String sexo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_SEXO));
                double peso = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_PESO_KG));
                int altura = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_ALTURA_CM));
                String condicionMedica = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_CONDICION_MEDICA));

                textViewNombreCompleto.setText(nombreCompleto);
                textViewEdad.setText(String.valueOf(edad));
                textViewSexo.setText(sexo);
                textViewPeso.setText(String.valueOf(peso));
                textViewAltura.setText(String.valueOf(altura));
                textViewCondicionMedica.setText(condicionMedica);
            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "No se encontraron datos del perfil.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
