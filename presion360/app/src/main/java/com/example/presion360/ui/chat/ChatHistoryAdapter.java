package com.example.presion360.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presion360.R;
import com.example.presion360.data.model.ChatSessionSummary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

    private List<ChatSessionSummary> chatSessionSummaries;
    private Context context;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

    public ChatHistoryAdapter(List<ChatSessionSummary> chatSessionSummaries, Context context) {
        this.chatSessionSummaries = chatSessionSummaries;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_history_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSessionSummary summary = chatSessionSummaries.get(position);
        holder.textViewPreview.setText(summary.getPreviewText());
        holder.textViewTimestamp.setText(sdf.format(new Date(summary.getStartTimestamp())));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra(ChatDetailActivity.EXTRA_SESSION_ID, summary.getSessionId());
            intent.putExtra(ChatDetailActivity.EXTRA_SESSION_PREVIEW, summary.getPreviewText()); // Para el título
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatSessionSummaries.size();
    }

    public void updateData(List<ChatSessionSummary> newSummaries) {
        this.chatSessionSummaries.clear();
        this.chatSessionSummaries.addAll(newSummaries);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPreview;
        TextView textViewTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPreview = itemView.findViewById(R.id.textViewChatSummaryPreview);
            textViewTimestamp = itemView.findViewById(R.id.textViewChatSummaryTimestamp);
        }
    }
}
