package com.example.presion360.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.presion360.data.model.ChatSessionSummary;
import com.example.presion360.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ChatHistoryAct"; // Consistente con tus logs

    private RecyclerView recyclerViewChatHistory;
    private TextView textViewNoChatHistory;
    private ChatHistoryAdapter chatHistoryAdapter;
    private List<ChatSessionSummary> chatSessionList;
    private DatabaseHelper dbHelper;
    private String currentUserEmail; // Variable de instancia para el email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate INICIADO");
        setContentView(R.layout.activity_chat_history);

        if (getApplicationContext() != null) {
            dbHelper = new DatabaseHelper(this);
            // Asegurarse de que this.currentUserEmail se inicialice aquí
            this.currentUserEmail = SessionManager.getInstance(getApplicationContext()).getCurrentUserEmail();
            Log.d(TAG, "onCreate - this.currentUserEmail recuperado via SessionManager: '" + this.currentUserEmail + "'");

            // Verificación directa (opcional, ya que los logs previos confirmaron que funciona)
            SharedPreferences directPrefs = getApplicationContext().getSharedPreferences("Presion360UserSession", Context.MODE_PRIVATE);
            String directEmailRead = directPrefs.getString("userEmail", "FALLBACK_DEFAULT_EMPTY");
            Log.d(TAG, "onCreate - Email leído DIRECTAMENTE de SharedPreferences: '" + directEmailRead + "'");
        } else {
            Log.e(TAG, "onCreate - getApplicationContext() es null. No se pudo inicializar dbHelper ni SessionManager.");
            this.currentUserEmail = ""; // Fallback si el contexto es nulo
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_chat_history));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewChatHistory = findViewById(R.id.recyclerViewChatHistory);
        textViewNoChatHistory = findViewById(R.id.textViewNoChatHistory);

        recyclerViewChatHistory.setLayoutManager(new LinearLayoutManager(this));
        chatSessionList = new ArrayList<>();
        chatHistoryAdapter = new ChatHistoryAdapter(chatSessionList, this);
        recyclerViewChatHistory.setAdapter(chatHistoryAdapter);

        // La lógica de carga se mueve a onResume o se llama explícitamente después de verificar el email.
        // No llamar a loadChatHistory() aquí directamente si depende de currentUserEmail y hay un bloque if/else abajo.

        Log.d(TAG, "onCreate FINALIZADO.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume INICIADO. Verificando y cargando historial para email: '" + this.currentUserEmail + "'");
        // Mover la lógica de decidir si cargar o mostrar error aquí,
        // ya que onResume se llama cada vez que la actividad se vuelve activa.

        if (TextUtils.isEmpty(this.currentUserEmail)) {
            // Re-intentar obtener el email si estaba vacío, por si acaso.
            if (getApplicationContext() != null) {
                 this.currentUserEmail = SessionManager.getInstance(getApplicationContext()).getCurrentUserEmail();
                 Log.w(TAG, "onResume - currentUserEmail estaba vacío, re-obtenido a: '" + this.currentUserEmail + "'");
            }
        }

        if (TextUtils.isEmpty(this.currentUserEmail)) {
            Log.e(TAG, "onResume - currentUserEmail SIGUE VACÍO. Mostrando error de usuario no identificado.");
            Toast.makeText(this, "Error al cargar el historial: Usuario no identificado.", Toast.LENGTH_LONG).show();
            textViewNoChatHistory.setText("Error: Usuario no identificado."); // Mensaje de error específico
            textViewNoChatHistory.setVisibility(View.VISIBLE);
            recyclerViewChatHistory.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "onResume - currentUserEmail es '" + this.currentUserEmail + "'. Procediendo a cargar historial.");
            loadChatHistory();
        }
    }

    private void loadChatHistory() {
        // this.currentUserEmail ya debería estar establecido correctamente antes de llamar a este método.
        Log.d(TAG, "loadChatHistory INICIADO. Email del usuario para la consulta: '" + this.currentUserEmail + "'");

        if (dbHelper == null || TextUtils.isEmpty(this.currentUserEmail)) {
            Log.e(TAG, "loadChatHistory - dbHelper es null o currentUserEmail está vacío. No se puede cargar.");
            // Asegurar que la UI refleje este estado si no se manejó en onResume
            chatSessionList.clear();
            if (chatHistoryAdapter!= null) chatHistoryAdapter.notifyDataSetChanged();
            textViewNoChatHistory.setText(TextUtils.isEmpty(this.currentUserEmail) ? "Error: Usuario no identificado." : "Error interno al cargar historial.");
            textViewNoChatHistory.setVisibility(View.VISIBLE);
            recyclerViewChatHistory.setVisibility(View.GONE);
            return;
        }

        List<ChatSessionSummary> sessions = dbHelper.getChatSessionsForUser(this.currentUserEmail);

        if (sessions != null && !sessions.isEmpty()) {
            Log.d(TAG, "Sesiones encontradas: " + sessions.size());
            chatSessionList.clear(); 
            chatSessionList.addAll(sessions);
            if (chatHistoryAdapter!= null) chatHistoryAdapter.notifyDataSetChanged();
            recyclerViewChatHistory.setVisibility(View.VISIBLE);
            textViewNoChatHistory.setVisibility(View.GONE);
        } else {
            Log.d(TAG, sessions == null ? "Lista de sesiones es NULL." : "No se encontraron sesiones de chat (lista vacía).");
            chatSessionList.clear();
            if (chatHistoryAdapter!= null) chatHistoryAdapter.notifyDataSetChanged();
            recyclerViewChatHistory.setVisibility(View.GONE);
            textViewNoChatHistory.setText(getString(R.string.no_chat_history_available)); // Usar string resource
            textViewNoChatHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); 
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
