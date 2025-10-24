package com.example.presion360.ui.chat;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presion360.R;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.data.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "extra_session_id";
    public static final String EXTRA_SESSION_PREVIEW = "extra_session_preview"; // Para el título

    private RecyclerView recyclerViewChatDetailMessages;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private DatabaseHelper dbHelper;
    private long sessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        dbHelper = new DatabaseHelper(this);
        sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1);
        String sessionPreview = getIntent().getStringExtra(EXTRA_SESSION_PREVIEW);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (sessionPreview != null && !sessionPreview.isEmpty()) {
                actionBar.setTitle("Chat: " + (sessionPreview.length() > 30 ? sessionPreview.substring(0, 30) + "..." : sessionPreview) );
            } else {
                actionBar.setTitle("Detalle de Chat");
            }
        }

        if (sessionId == -1) {
            Toast.makeText(this, "Error: No se pudo cargar la sesión de chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerViewChatDetailMessages = findViewById(R.id.recyclerViewChatDetailMessages);
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList); // Usamos el mismo ChatAdapter

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewChatDetailMessages.setLayoutManager(layoutManager);
        recyclerViewChatDetailMessages.setAdapter(chatAdapter);

        loadChatMessages();
    }

    private void loadChatMessages() {
        List<ChatMessage> messages = dbHelper.getMessagesForSession(sessionId);
        if (messages != null && !messages.isEmpty()) {
            chatMessageList.addAll(messages);
            chatAdapter.notifyDataSetChanged();
            if (chatAdapter.getItemCount() > 0) {
                recyclerViewChatDetailMessages.scrollToPosition(chatAdapter.getItemCount() - 1); // Scroll al último mensaje
            }
        } else {
            Toast.makeText(this, "No se encontraron mensajes para esta sesión.", Toast.LENGTH_SHORT).show();
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
