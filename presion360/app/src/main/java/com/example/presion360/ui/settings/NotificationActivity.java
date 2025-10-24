package com.example.presion360.ui.settings;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.presion360.R;
import com.example.presion360.utils.NotificationReceiver;

import java.util.Calendar;

public class NotificationActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button saveButton;
    private TextView statusTextView;

    public static final String PREFS_NAME = "PressureReminderPrefs";
    public static final String PREF_HOUR = "reminderHour";
    public static final String PREF_MINUTE = "reminderMinute";
    public static final String PREF_IS_SET = "isReminderSet";

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            checkAndSchedule();
        } else {
            Toast.makeText(this, "El permiso de notificación es necesario para los recordatorios.", Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        timePicker = findViewById(R.id.timePicker);
        saveButton = findViewById(R.id.saveButton);
        statusTextView = findViewById(R.id.statusTextView);

        loadReminderState();

        saveButton.setOnClickListener(v -> checkPermissionAndSchedule());
    }

    private void checkPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        checkAndSchedule();
    }

    private void checkAndSchedule() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "Por favor, autoriza el permiso para programar alarmas exactas.", Toast.LENGTH_LONG).show();
        } else {
            saveAndSchedule();
        }
    }

    private void saveAndSchedule() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        saveReminder(hour, minute);
        scheduleNotification(hour, minute);

        updateStatusText(hour, minute);
        Toast.makeText(this, "Recordatorio guardado.", Toast.LENGTH_SHORT).show();
    }

    private void loadReminderState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isSet = prefs.getBoolean(PREF_IS_SET, false);

        if (isSet) {
            int hour = prefs.getInt(PREF_HOUR, -1);
            int minute = prefs.getInt(PREF_MINUTE, -1);

            if (hour != -1 && minute != -1) {
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);
                updateStatusText(hour, minute);
            }
        }
    }

    private void saveReminder(int hour, int minute) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(PREF_HOUR, hour);
        editor.putInt(PREF_MINUTE, minute);
        editor.putBoolean(PREF_IS_SET, true);
        editor.apply();
    }

    private void scheduleNotification(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void updateStatusText(int hour, int minute) {
        statusTextView.setText(String.format("Recordatorio programado para las %02d:%02d", hour, minute));
    }
}
