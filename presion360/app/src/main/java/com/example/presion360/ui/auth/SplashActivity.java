package com.example.presion360.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;
import com.example.presion360.ui.main.MainNavigationActivity;
import com.example.presion360.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        videoView = findViewById(R.id.splashVideo);

        try {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splashvideo);
            videoView.setVideoURI(videoUri);

            videoView.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setVolume(0f, 0f);
                videoView.seekTo(0);
                videoView.start();
            });

            videoView.setOnCompletionListener(mp -> {
                navigateToNextActivity();
            });

            videoView.requestFocus();
            videoView.setZOrderOnTop(true);

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar o reproducir el video de splash: " + e.getMessage(), e);
            navigateToNextActivity(); // Proceder incluso si hay error de video
        }
    }

    private void navigateToNextActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(RegisterActivity.KEY_IS_LOGGED_IN, false);
        boolean shouldRememberMe = sharedPreferences.getBoolean(RegisterActivity.KEY_SHOULD_REMEMBER_ME, false);
        String userEmail = sharedPreferences.getString(RegisterActivity.KEY_USER_EMAIL, null); // Leer el email guardado

        Log.d(TAG, "Verificando estado de sesión: isLoggedIn=" + isLoggedIn + ", shouldRememberMe=" + shouldRememberMe + ", userEmail=" + userEmail);

        Intent intent;
        if (isLoggedIn && shouldRememberMe && userEmail != null) {
            Log.d(TAG, "Usuario recordado y logueado. Email: " + userEmail + ". Navegando a MainNavigationActivity.");
            // Asegurarse de que SessionManager tenga el email actual
            SessionManager.getInstance(getApplicationContext()).saveCurrentUserEmail(userEmail);
            intent = new Intent(SplashActivity.this, MainNavigationActivity.class);
        } else {
            Log.d(TAG, "Usuario no logueado o no eligió ser recordado. Navegando a LoginActivity.");
            // Si no se va a loguear automáticamente, es buena práctica limpiar la sesión por si acaso
            // (aunque si isLoggedIn es false, SessionManager no debería tener un email incorrecto).
            // SessionManager.getInstance(getApplicationContext()).clearSession(); // Considerar esto en el logout
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        // Añadir flags para limpiar la pila de actividades anteriores, asegurando que no se pueda volver al Splash
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finalizar SplashActivity
    }

    @Override
    protected void onDestroy() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }
}
