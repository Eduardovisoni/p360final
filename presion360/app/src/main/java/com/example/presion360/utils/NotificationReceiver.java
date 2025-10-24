package com.example.presion360.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.presion360.R;
import com.example.presion360.ui.auth.SplashActivity;
import com.example.presion360.ui.settings.NotificationActivity;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "pressure_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorio de Presión Arterial",
                    NotificationManager.IMPORTANCE_HIGH // Cambiado para que vibre
            );
            channel.setDescription("Canal para recordatorios de toma de presión arterial.");
            notificationManager.createNotificationChannel(channel);
        }

        // Cambiado para que abra SplashActivity y se valide la sesión
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Recordatorio de Presión 360")
                .setContentText("Es hora de registrar tu presión arterial.")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Cambiado para que vibre
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());

        // Re-programar la alarma para el día siguiente
        rescheduleAlarm(context);
    }

    private void rescheduleAlarm(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NotificationActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int hour = prefs.getInt(NotificationActivity.PREF_HOUR, -1);
        int minute = prefs.getInt(NotificationActivity.PREF_MINUTE, -1);

        if (hour != -1 && minute != -1) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // Programar para el día siguiente
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
