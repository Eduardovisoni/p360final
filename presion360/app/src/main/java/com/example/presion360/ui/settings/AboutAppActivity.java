package com.example.presion360.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        // Habilitar el botón de regreso en la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Manejar el botón de regreso de la barra de acción
        onBackPressed();
        return true;
    }
}
