package com.example.presion360.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView; // Para el error del Spinner
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.ui.terms.TermsActivity;
import com.example.presion360.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    // Constantes para SharedPreferences (idealmente en una clase AppPreferences o similar)
    public static final String SESSION_PREFS_NAME = "Presion360UserSession";
    public static final String KEY_USER_EMAIL = "userEmail";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_SHOULD_REMEMBER_ME = "shouldRememberMe";

    private EditText editTextNombreCompleto, editTextEdad, editTextPeso, editTextAltura, editTextCondicionMedica;
    private Spinner spinnerSexo;
    private Button buttonGuardar;
    private DatabaseHelper dbHelper;
    private String currentUserEmail; // Para almacenar el email del usuario actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_EMAIL")) {
            currentUserEmail = intent.getStringExtra("USER_EMAIL");
        } else {
            Toast.makeText(this, "Error: No se ha proporcionado la información del usuario.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "USER_EMAIL no fue pasado a RegisterActivity.");
            finish(); 
            return; 
        }

        editTextNombreCompleto = findViewById(R.id.editTextNombreCompleto);
        editTextEdad = findViewById(R.id.editTextEdad);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        editTextPeso = findViewById(R.id.editTextPeso);
        editTextAltura = findViewById(R.id.editTextAltura);
        editTextCondicionMedica = findViewById(R.id.editTextCondicionMedica);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarDatos()) {
                    guardarDatosDePerfilLocal();
                }
            }
        });
    }

    private void guardarDatosDePerfilLocal() {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar al usuario para guardar el perfil.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "currentUserEmail es nulo o vacío al intentar guardar perfil.");
            return;
        }

        String nombreCompleto = editTextNombreCompleto.getText().toString().trim();
        String edadStr = editTextEdad.getText().toString().trim();
        String sexoSeleccionado = spinnerSexo.getSelectedItem().toString();
        String pesoStr = editTextPeso.getText().toString().trim();
        String alturaStr = editTextAltura.getText().toString().trim();
        String condicionMedica = editTextCondicionMedica.getText().toString().trim();

        int edad;
        double pesoKg;
        int alturaCm;

        try {
            edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            editTextEdad.setError("Ingrese una edad válida.");
            editTextEdad.requestFocus();
            Log.w(TAG, "Error al parsear edad: " + edadStr, e);
            return;
        }

        try {
            pesoKg = Double.parseDouble(pesoStr);
        } catch (NumberFormatException e) {
            editTextPeso.setError("Ingrese un peso válido.");
            editTextPeso.requestFocus();
            Log.w(TAG, "Error al parsear peso: " + pesoStr, e);
            return;
        }

        try {
            alturaCm = Integer.parseInt(alturaStr);
        } catch (NumberFormatException e) {
            editTextAltura.setError("Ingrese una altura válida.");
            editTextAltura.requestFocus();
            Log.w(TAG, "Error al parsear altura: " + alturaStr, e);
            return;
        }

        buttonGuardar.setEnabled(false);

        if (dbHelper.addOrUpdateProfile(currentUserEmail, nombreCompleto, edad, sexoSeleccionado, pesoKg, alturaCm, condicionMedica)) {
            Toast.makeText(this, "Los datos de su perfil han sido guardados satisfactoriamente.", Toast.LENGTH_LONG).show();
            
            // Establecer la sesión del usuario AHORA
            Log.d(TAG, "Perfil guardado. Estableciendo sesión para: " + currentUserEmail);
            SessionManager.getInstance(getApplicationContext()).saveCurrentUserEmail(currentUserEmail);
            
            SharedPreferences sharedPreferences = getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putBoolean(KEY_SHOULD_REMEMBER_ME, true); // Por defecto, recordar al nuevo usuario
            // El email ya se guarda a través de SessionManager, pero si se necesitara directamente:
            // editor.putString(KEY_USER_EMAIL, currentUserEmail);
            editor.apply();
            Log.d(TAG, "SessionManager y SharedPreferences actualizados. isLoggedIn=true, shouldRememberMe=true");

            Intent termsIntent = new Intent(RegisterActivity.this, TermsActivity.class);
            // Opcional: Pasar el email a TermsActivity si es necesario, aunque ya debería estar en SessionManager
            // termsIntent.putExtra("USER_EMAIL", currentUserEmail); 
            startActivity(termsIntent);
            finish(); // Finalizar RegisterActivity
        } else {
            Toast.makeText(this, "Se produjo un error al guardar los datos de su perfil. Por favor, inténtelo nuevamente.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error al guardar perfil en SQLite para el usuario: " + currentUserEmail);
            buttonGuardar.setEnabled(true); 
        }
    }

    private boolean validarDatos() {
        String nombre = editTextNombreCompleto.getText().toString().trim();
        String edadStr = editTextEdad.getText().toString().trim();
        String pesoStr = editTextPeso.getText().toString().trim();
        String alturaStr = editTextAltura.getText().toString().trim();
        int sexoPosicion = spinnerSexo.getSelectedItemPosition();

        if (TextUtils.isEmpty(nombre)) {
            editTextNombreCompleto.setError("El nombre es obligatorio");
            editTextNombreCompleto.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(edadStr)) {
            editTextEdad.setError("La edad es obligatoria");
            editTextEdad.requestFocus();
            return false;
        }
        try {
            Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            editTextEdad.setError("Ingrese una edad válida (número).");
            editTextEdad.requestFocus();
            return false;
        }

        if (sexoPosicion == 0) { 
            Toast.makeText(this, "Por favor, selecciona tu sexo.", Toast.LENGTH_SHORT).show();
            spinnerSexo.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pesoStr)) {
            editTextPeso.setError("El peso es obligatorio");
            editTextPeso.requestFocus();
            return false;
        }
        try {
            Double.parseDouble(pesoStr);
        } catch (NumberFormatException e) {
            editTextPeso.setError("Ingrese un peso válido (número).");
            editTextPeso.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(alturaStr)) {
            editTextAltura.setError("La altura es obligatoria");
            editTextAltura.requestFocus();
            return false;
        }
        try {
            Integer.parseInt(alturaStr);
        } catch (NumberFormatException e) {
            editTextAltura.setError("Ingrese una altura válida (número).");
            editTextAltura.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
