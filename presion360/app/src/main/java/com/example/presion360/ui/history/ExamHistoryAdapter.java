package com.example.presion360.ui.history;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log; // Importación añadida para Logs
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.presion360.R;
import com.example.presion360.data.model.ExamRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList; // Asegurarse de que ArrayList está importado
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExamHistoryAdapter extends RecyclerView.Adapter<ExamHistoryAdapter.ViewHolder> {

    private List<ExamRecord> examRecords;
    private Context context;
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final String ADAPTER_TAG = "ExamHistoryAdapter"; // Tag para logs del adaptador

    public ExamHistoryAdapter(List<ExamRecord> examRecords, Context context) {
        // Es mejor crear una nueva lista para asegurar que el adaptador tenga su propia copia manejable
        this.examRecords = new ArrayList<>(examRecords != null ? examRecords : new ArrayList<>());
        this.context = context;
        Log.d(ADAPTER_TAG, "Constructor - initial items: " + this.examRecords.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(ADAPTER_TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam_history_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(ADAPTER_TAG, "onBindViewHolder - position: " + position);
        if (examRecords == null || position < 0 || position >= examRecords.size()) {
            Log.e(ADAPTER_TAG, "onBindViewHolder - Invalid position or null examRecords. Position: " + position + ", Size: " + (examRecords == null ? "null" : examRecords.size()));
            return;
        }
        ExamRecord record = examRecords.get(position);

        String category = record.getCategory();
        if (category == null || category.isEmpty()) {
            category = "Resultado no disponible";
        }
        final String finalCategory = category;

        holder.textViewExamCategory.setText(finalCategory);

        int categoryColor = Color.BLACK;
        if (finalCategory.contains("Hipotensión")) {
            categoryColor = Color.BLUE;
        } else if (finalCategory.equalsIgnoreCase("Normal")) {
            categoryColor = Color.parseColor("#4CAF50");
        } else if (finalCategory.equalsIgnoreCase("Elevada")) {
            categoryColor = Color.parseColor("#FFEB3B");
        } else if (finalCategory.contains("Hipertensión Nivel 1")) {
            categoryColor = Color.parseColor("#FF9800");
        } else if (finalCategory.contains("Hipertensión Nivel 2")) {
            categoryColor = Color.parseColor("#F44336");
        } else if (finalCategory.contains("Crisis Hipertensiva")) {
            categoryColor = Color.parseColor("#B71C1C");
        }
        holder.textViewExamCategory.setTextColor(categoryColor);

        final String actualTimestamp = dateTimeFormatter.format(new Date(record.getTimestamp()));
        holder.textViewExamActualTimestamp.setText(actualTimestamp);

        String values = String.format(Locale.getDefault(),
                "Sistólica: %d mmHg, Diastólica: %d mmHg, Pulso: %d bpm",
                record.getSystolic(), record.getDiastolic(), record.getPulse());
        holder.textViewExamValues.setText(values);

        String notesText = record.getNotes();
        if (notesText != null && !notesText.isEmpty()) {
            holder.textViewExamNotes.setText(context.getString(R.string.notes_prefix) + " " + notesText);
            holder.textViewExamNotes.setVisibility(View.VISIBLE);
        } else {
            holder.textViewExamNotes.setVisibility(View.GONE);
            notesText = "Ninguna";
        }
        final String finalNotes = notesText;

        holder.buttonCopyForAI.setOnClickListener(v -> {
            String examDataToCopy = String.format(Locale.getDefault(),
                    "Analiza estos datos y dame recomendaciones de que hacer: Categoría: %s, Fecha y Hora: %s, Valores: Sistólica %d mmHg, Diastólica %d mmHg, Pulso %d bpm, Notas: %s",
                    finalCategory,
                    actualTimestamp,
                    record.getSystolic(),
                    record.getDiastolic(),
                    record.getPulse(),
                    finalNotes);

            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ExamData", examDataToCopy);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, context.getString(R.string.toast_exam_data_copied_for_ai), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error al copiar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = examRecords == null ? 0 : examRecords.size();
        Log.d(ADAPTER_TAG, "getItemCount() called, returning: " + count); // Log crucial aquí
        return count;
    }

    public void updateData(List<ExamRecord> newRecords) {
        Log.d(ADAPTER_TAG, "updateData - old size before clear: " + (this.examRecords != null ? this.examRecords.size() : "null list"));
        if (this.examRecords == null) { // Por si acaso, aunque el constructor debería inicializarlo
            this.examRecords = new ArrayList<>();
        }
        this.examRecords.clear();
        Log.d(ADAPTER_TAG, "updateData - size after clear: " + this.examRecords.size());
        if (newRecords != null) {
            this.examRecords.addAll(newRecords);
        }
        Log.d(ADAPTER_TAG, "updateData - new size after addAll: " + this.examRecords.size());
        notifyDataSetChanged();
        Log.d(ADAPTER_TAG, "updateData - notifyDataSetChanged() called. Current getItemCount() would be: " + getItemCount());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewExamCategory;
        TextView textViewExamActualTimestamp;
        TextView textViewExamValues;
        TextView textViewExamNotes;
        Button buttonCopyForAI;

        ViewHolder(View itemView) {
            super(itemView);
            textViewExamCategory = itemView.findViewById(R.id.textViewExamCategory);
            textViewExamActualTimestamp = itemView.findViewById(R.id.textViewExamActualTimestamp);
            textViewExamValues = itemView.findViewById(R.id.textViewExamValues);
            textViewExamNotes = itemView.findViewById(R.id.textViewExamNotes);
            buttonCopyForAI = itemView.findViewById(R.id.buttonCopyForAI);
        }
    }
}
