package com.example.presion360.ui.history;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.presion360.R;
import com.example.presion360.ui.chat.ChatHistoryActivity;

public class HistoryFragment extends Fragment {

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonChatHistory = view.findViewById(R.id.buttonChatHistory);
        Button buttonExamsHistory = view.findViewById(R.id.buttonExamsHistory);

        buttonChatHistory.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ChatHistoryActivity.class);
                startActivity(intent);
            }
        });

        buttonExamsHistory.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ExamsHistoryActivity.class);
                startActivity(intent);
            }
        });
    }
}
