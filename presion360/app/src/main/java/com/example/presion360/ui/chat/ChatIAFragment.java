package com.example.presion360.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presion360.R;
import com.example.presion360.data.api.ApiClient;
import com.example.presion360.data.api.N8nApiService;
import com.example.presion360.data.db.DatabaseHelper;
import com.example.presion360.data.model.ChatMessage;
import com.example.presion360.data.model.ChatRequest;
import com.example.presion360.data.model.ChatResponse;
import com.example.presion360.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatIAFragment extends Fragment {

    private static final String TAG = "ChatIAFragment";

    private RecyclerView recyclerViewChatMessages;
    private EditText editTextChatMessage;
    private Button buttonSendChatMessage;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;

    private ExecutorService executorService;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private DatabaseHelper dbHelper;
    private long currentSessionId = -1;
    private String currentUserEmail;

    private N8nApiService n8nApiService;

    public ChatIAFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
            this.currentUserEmail = SessionManager.getInstance(getContext()).getCurrentUserEmail();
        } else {
            this.currentUserEmail = "";
        }
        n8nApiService = ApiClient.getClient().create(N8nApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_ia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewChatMessages = view.findViewById(R.id.recyclerViewChatMessages);
        editTextChatMessage = view.findViewById(R.id.editTextChatMessage);
        buttonSendChatMessage = view.findViewById(R.id.buttonSendChatMessage);

        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        recyclerViewChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChatMessages.setAdapter(chatAdapter);
        buttonSendChatMessage.setOnClickListener(v -> handleSendMessage());
    }

    private void handleSendMessage() {
        if (getContext() == null) {
            Toast.makeText(getContext(), "Error: No se puede enviar el mensaje.", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailForMessage = SessionManager.getInstance(getContext()).getCurrentUserEmail();
        String messageText = editTextChatMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(getContext(), "Escribe un mensaje.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(emailForMessage) || dbHelper == null) {
            Toast.makeText(getContext(), "Error: Usuario no identificado. El chat no se guardará.", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentSessionId == -1) {
            String preview = messageText.length() > 100 ? messageText.substring(0, 100) : messageText;
            currentSessionId = dbHelper.startNewChatSession(emailForMessage, preview);
            if (currentSessionId == -1) {
                Toast.makeText(getContext(), "Error al iniciar y guardar la sesión de chat.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ChatMessage userMessage = new ChatMessage(messageText, true);
        addMessageToChat(userMessage, true);
        dbHelper.addChatMessageToSession(currentSessionId, userMessage.getMessage(), true, userMessage.getTimestamp());
        editTextChatMessage.setText("");

        getResponseFromN8n(messageText);
    }

    private void getResponseFromN8n(String userQuestion) {
        if (getContext() == null) {
            addMessageToChat(new ChatMessage("Error: No se pudo conectar (contexto nulo).", false), true);
            return;
        }

        ChatMessage thinkingMessage = new ChatMessage("El asistente está pensando...", false);
        addMessageToChat(thinkingMessage, true);
        String userEmail = SessionManager.getInstance(getContext()).getCurrentUserEmail();

        if (TextUtils.isEmpty(userEmail)) {
            removeLastBotMessage();
            addMessageToChat(new ChatMessage("Error: Usuario no identificado. No se puede usar la IA.", false), true);
            return;
        }

        // Creación explícita del objeto y asignación de campos
        ChatRequest request = new ChatRequest();
        request.message = userQuestion;
        request.user = userEmail;

        Call<ChatResponse> call = n8nApiService.sendMessage(request);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                removeLastBotMessage();
                ChatMessage botMessageToSave;

                if (response.isSuccessful() && response.body() != null) {
                    String botResponseText = response.body().getResponse();
                    if (botResponseText == null || botResponseText.trim().isEmpty()) {
                        botResponseText = "El asistente no ha respondido. Intente de nuevo.";
                    }
                    botMessageToSave = new ChatMessage(botResponseText.trim(), false);
                } else {
                    String errorDetails = "Código: " + response.code();
                    Log.e(TAG, "Error en la respuesta de n8n. " + errorDetails);
                    botMessageToSave = new ChatMessage("Error " + response.code() + ": No se pudo obtener una respuesta del asistente.", false);
                }

                addMessageToChat(botMessageToSave, true);
                if (dbHelper != null && currentSessionId != -1) {
                    dbHelper.addChatMessageToSession(currentSessionId, botMessageToSave.getMessage(), false, botMessageToSave.getTimestamp());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                removeLastBotMessage();
                Log.e(TAG, "Fallo en la llamada a n8n: " + t.getMessage(), t);
                ChatMessage errorMessage = new ChatMessage("Error de conexión. Revise su conexión a internet.", false);
                addMessageToChat(errorMessage, true);
            }
        });
    }

    private void addMessageToChat(ChatMessage message, boolean scrollToBottom) {
        mainThreadHandler.post(() -> {
            if (chatAdapter == null || recyclerViewChatMessages == null || getContext() == null) {
                return;
            }
            chatAdapter.addMessage(message);
            if (scrollToBottom && chatAdapter.getItemCount() > 0) {
                recyclerViewChatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void removeLastBotMessage() {
        mainThreadHandler.post(() -> {
            if (chatAdapter == null || getContext() == null || chatMessageList.isEmpty()) return;
            int lastIndex = chatMessageList.size() - 1;
            ChatMessage lastMessage = chatMessageList.get(lastIndex);
            if (!lastMessage.isUserMessage() && lastMessage.getMessage().equals("El asistente está pensando...")) {
                chatMessageList.remove(lastIndex);
                chatAdapter.notifyItemRemoved(lastIndex);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewChatMessages = null;
        editTextChatMessage = null;
        buttonSendChatMessage = null;
        chatAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
