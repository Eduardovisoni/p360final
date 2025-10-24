package com.example.presion360.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class SessionManager {

    private static final String PREF_NAME = "Presion360UserSession";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String DEFAULT_EMAIL = "";
    private static final String TAG = "SessionManager"; // TAG para logs

    private static SessionManager instance;
    private SharedPreferences sharedPreferences;

    private SessionManager(Context context) {
        Log.d(TAG, "SessionManager CONSTRUCTOR CALLED. HashCode: " + this.hashCode() + ", Context: " + context.getClass().getSimpleName());
        Context appContext = context.getApplicationContext();
        sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "getInstance(): instance is NULL, creating NEW. Called from Context: " + context.getClass().getSimpleName());
            instance = new SessionManager(context);
        } else {
            Log.d(TAG, "getInstance(): returning EXISTING instance. HashCode: " + instance.hashCode() + ", Called from Context: " + context.getClass().getSimpleName());
        }
        return instance;
    }

    public void saveCurrentUserEmail(String email) {
        SharedPreferences.Editor localEditor = sharedPreferences.edit();
        String emailToSave = (email == null) ? DEFAULT_EMAIL : email;
        localEditor.putString(KEY_USER_EMAIL, emailToSave);
        boolean success = localEditor.commit();
        Log.d(TAG, "saveCurrentUserEmail: '" + emailToSave + "', Commit success: " + success);
    }

    public String getCurrentUserEmail() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, DEFAULT_EMAIL);
        // Log.d(TAG, "getCurrentUserEmail: '" + email + "' (Instance HashCode: " + this.hashCode() + ")"); // Log intensivo si es necesario
        return email;
    }

    public boolean isLoggedIn() {
        String userEmail = getCurrentUserEmail();
        return !TextUtils.isEmpty(userEmail);
    }

    public void clearSession() {
        SharedPreferences.Editor localEditor = sharedPreferences.edit();
        localEditor.remove(KEY_USER_EMAIL);
        boolean success = localEditor.commit();
        Log.d(TAG, "clearSession, Commit success: " + success + " (Instance HashCode: " + this.hashCode() + ")");
    }
}
