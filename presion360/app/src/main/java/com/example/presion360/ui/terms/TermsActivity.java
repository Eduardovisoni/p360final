package com.example.presion360.ui.terms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.presion360.R;
import com.example.presion360.ui.main.MainNavigationActivity;

public class TermsActivity extends AppCompatActivity {

    private static final String TAG = "TermsActivity";
    private Button buttonAcceptTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        buttonAcceptTerms = findViewById(R.id.buttonAcceptTerms);

        buttonAcceptTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Términos aceptados. Navegando a MainNavigationActivity y limpiando pila anterior.");
                Intent intent = new Intent(TermsActivity.this, MainNavigationActivity.class);
                // Añadir flags para limpiar la pila de actividades anteriores
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Finalizar TermsActivity
            }
        });
    }
}
