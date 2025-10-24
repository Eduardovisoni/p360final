package com.example.presion360.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log; // Para depuración adicional si es necesario

import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.ui.auth.RegisterActivity;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText editTextEmail, editTextPassword;
    private TextInputLayout textFieldEmail, textFieldPassword;
    private Button buttonRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        textFieldEmail = findViewById(R.id.textFieldEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        textFieldPassword = findViewById(R.id.textFieldPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        // Limpiar errores previos
        textFieldEmail.setError(null);
        textFieldPassword.setError(null);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(email)) {
            textFieldEmail.setError("El correo electrónico es obligatorio.");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textFieldEmail.setError("Ingrese un correo electrónico válido.");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            textFieldPassword.setError("La contraseña es obligatoria.");
            editTextPassword.requestFocus();
            return;
        }

        // Intentar registrar al usuario
        if (dbHelper.addUser(email, password)) {
            Toast.makeText(this, "Registro completado exitosamente. Por favor, complete su perfil.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            intent.putExtra("USER_EMAIL", email); // Pasar el email a RegisterActivity
            startActivity(intent);
        } else {
            if (dbHelper.checkUserExists(email)) {
                Toast.makeText(this, "Este correo electrónico ya ha sido registrado. Por favor, intente con otro.", Toast.LENGTH_LONG).show();
                textFieldEmail.setError("Este correo ya está registrado.");
                editTextEmail.requestFocus();
            } else {
                Toast.makeText(this, "Error al registrar la cuenta. Por favor, inténtelo de nuevo más tarde.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error no especificado al intentar registrar el usuario: " + email);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close(); 
        }
        super.onDestroy();
    }
}
