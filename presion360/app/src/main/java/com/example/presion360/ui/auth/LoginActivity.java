package com.example.presion360.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.ui.main.MainActivity;
import com.example.presion360.ui.main.MainNavigationActivity;
import com.example.presion360.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editTextEmailLogin, editTextPasswordLogin;
    private TextInputLayout textFieldEmailLogin, textFieldPasswordLogin;
    private Button buttonLogin;
    private TextView textViewGoToRegister;
    private CheckBox checkBoxRememberMe; // Añadido para "Recordarme"
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper = new DatabaseHelper(this);

        textFieldEmailLogin = findViewById(R.id.textFieldEmailLogin);
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        textFieldPasswordLogin = findViewById(R.id.textFieldPasswordLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe); // Referencia al CheckBox

        buttonLogin.setOnClickListener(v -> intentarAcceso());
        textViewGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // Quizás debería ser RegisterActivity?
            startActivity(intent);
        });
    }

    private void intentarAcceso() {
        textFieldEmailLogin.setError(null);
        textFieldPasswordLogin.setError(null);
        String email = editTextEmailLogin.getText().toString().trim();
        String password = editTextPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            textFieldEmailLogin.setError(getString(R.string.login_error_email_required));
            editTextEmailLogin.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textFieldEmailLogin.setError(getString(R.string.login_error_invalid_email));
            editTextEmailLogin.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            textFieldPasswordLogin.setError(getString(R.string.login_error_password_required));
            editTextPasswordLogin.requestFocus();
            return;
        }

        // Nueva validación: Verificar si el usuario existe
        if (!dbHelper.checkUserExists(email)) {
            Toast.makeText(this, R.string.login_error_user_not_registered, Toast.LENGTH_LONG).show();
            textFieldEmailLogin.setError(getString(R.string.login_error_user_not_registered));
            editTextEmailLogin.requestFocus();
            Log.w(TAG, "Intento de acceso para usuario no registrado: " + email);
            return;
        }

        Log.d(TAG, "Intentando acceso con email: " + email);
        if (dbHelper.checkUserCredentials(email, password)) {
            Log.d(TAG, "Credenciales válidas para: " + email);

            SessionManager.getInstance(getApplicationContext()).saveCurrentUserEmail(email);
            Log.d(TAG, "Email guardado usando SessionManager: " + email);

            SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(RegisterActivity.KEY_IS_LOGGED_IN, true);
            
            if (checkBoxRememberMe.isChecked()) {
                editor.putBoolean(RegisterActivity.KEY_SHOULD_REMEMBER_ME, true);
                Log.d(TAG, RegisterActivity.KEY_SHOULD_REMEMBER_ME + " guardado como true.");
            } else {
                editor.putBoolean(RegisterActivity.KEY_SHOULD_REMEMBER_ME, false);
                Log.d(TAG, RegisterActivity.KEY_SHOULD_REMEMBER_ME + " guardado como false.");
            }
            editor.apply(); 
            Log.d(TAG, RegisterActivity.KEY_IS_LOGGED_IN + " guardado como true en " + RegisterActivity.SESSION_PREFS_NAME);

            Toast.makeText(this, R.string.login_success_message, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginActivity.this, MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.login_error_incorrect_credentials, Toast.LENGTH_LONG).show();
            editTextPasswordLogin.setText("");
            editTextPasswordLogin.requestFocus();
            Log.w(TAG, "Intento de acceso fallido para: " + email);
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
