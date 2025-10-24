package com.example.presion360.ui.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.presion360.R;
import com.example.presion360.ui.auth.LoginActivity;
import com.example.presion360.ui.auth.RegisterActivity;
import com.example.presion360.utils.SessionManager;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonUserProfile = view.findViewById(R.id.buttonUserProfile);
        Button buttonSendFeedback = view.findViewById(R.id.buttonSendFeedback);
        Button buttonShareApp = view.findViewById(R.id.buttonShareApp);
        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        Button buttonAboutApp = view.findViewById(R.id.buttonAboutApp);
        Button buttonNotification = view.findViewById(R.id.buttonNotification);

        buttonUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            startActivity(intent);
        });

        buttonSendFeedback.setOnClickListener(v -> sendFeedbackEmail());
        buttonShareApp.setOnClickListener(v -> shareApp());
        buttonLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        buttonAboutApp.setOnClickListener(v -> { // NUEVO
            Intent intent = new Intent(getActivity(), AboutAppActivity.class);
            startActivity(intent);
        });

        buttonNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void sendFeedbackEmail() {
        if (getContext() == null) return;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Solo las apps de correo electrónico deben manejar esto
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.settings_feedback_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_feedback_subject));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No hay ninguna aplicación de correo instalada.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareApp() {
        if (getContext() == null) return;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_app_text));
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        try {
            startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No se puede compartir en este momento.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        if (getContext() == null) {
            Log.e(TAG, "Contexto nulo al intentar mostrar diálogo de logout.");
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.settings_logout_confirm_title)
                .setMessage(R.string.settings_logout_confirm_message)
                .setPositiveButton(R.string.settings_logout_confirm_yes, (dialog, which) -> {
                    Log.d(TAG, "Cierre de sesión confirmado por el usuario.");
                    // Limpiar la sesión del usuario usando SessionManager
                    SessionManager.getInstance(requireContext()).clearSession();
                    Log.d(TAG, "SessionManager: Sesión de usuario limpiada.");

                    // Limpiar SharedPreferences consistentes
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences(RegisterActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(RegisterActivity.KEY_IS_LOGGED_IN, false);
                    editor.putBoolean(RegisterActivity.KEY_SHOULD_REMEMBER_ME, false); // También resetear "recordarme"
                    // Opcionalmente, también limpiar el email guardado, aunque SessionManager debería ser la fuente primaria
                    // editor.remove(RegisterActivity.KEY_USER_EMAIL);
                    editor.apply();
                    Log.d(TAG, "SharedPreferences: " + RegisterActivity.KEY_IS_LOGGED_IN + "=false, " + RegisterActivity.KEY_SHOULD_REMEMBER_ME + "=false");

                    // Navegar a LoginActivity y limpiar el stack de actividades
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finishAffinity(); // Cierra esta actividad y todas las afines en la tarea actual
                    }
                })
                .setNegativeButton(R.string.settings_logout_confirm_no, (dialog, which) -> {
                    Log.d(TAG, "Cierre de sesión cancelado por el usuario.");
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Opcional: añadir un ícono al diálogo
                .show();
    }
}
