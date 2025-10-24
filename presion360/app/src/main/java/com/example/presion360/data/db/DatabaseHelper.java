package com.example.presion360.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.presion360.data.model.ChatMessage;
import com.example.presion360.data.model.ChatSessionSummary;
import com.example.presion360.data.model.ExamRecord;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "presion360.db";
    private static final int DATABASE_VERSION = 4;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PROFILES = "profiles";
    public static final String TABLE_CHAT_SESSIONS = "chat_sessions";
    public static final String TABLE_CHAT_MESSAGES = "chat_messages";
    public static final String TABLE_EXAMS = "exams";

    // Users Table Columns
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD_HASH = "password_hash";

    // Profiles Table Columns
    public static final String COLUMN_PROFILE_ID = "_id";
    public static final String COLUMN_PROFILE_USER_EMAIL_FK = "user_email";
    public static final String COLUMN_PROFILE_NOMBRE_COMPLETO = "nombre_completo";
    public static final String COLUMN_PROFILE_EDAD = "edad";
    public static final String COLUMN_PROFILE_SEXO = "sexo";
    public static final String COLUMN_PROFILE_PESO_KG = "peso_kg";
    public static final String COLUMN_PROFILE_ALTURA_CM = "altura_cm";
    public static final String COLUMN_PROFILE_CONDICION_MEDICA = "condicion_medica";

    // Chat Sessions Table Columns
    public static final String COLUMN_CHAT_SESSION_ID = "_id";
    public static final String COLUMN_CHAT_SESSION_USER_EMAIL_FK = "user_email";
    public static final String COLUMN_CHAT_SESSION_START_TIMESTAMP = "start_timestamp";
    public static final String COLUMN_CHAT_SESSION_PREVIEW = "preview_text";

    // Chat Messages Table Columns
    public static final String COLUMN_CHAT_MESSAGE_ID = "_id";
    public static final String COLUMN_CHAT_MESSAGE_SESSION_ID_FK = "session_id";
    public static final String COLUMN_CHAT_MESSAGE_TEXT = "message_text";
    public static final String COLUMN_CHAT_MESSAGE_IS_USER = "is_user_message";
    public static final String COLUMN_CHAT_MESSAGE_TIMESTAMP = "message_timestamp";

    // Exams Table Columns
    public static final String COLUMN_EXAM_ID = "_id";
    public static final String COLUMN_EXAM_USER_EMAIL_FK = "user_email";
    public static final String COLUMN_EXAM_TIMESTAMP = "timestamp";
    public static final String COLUMN_EXAM_SYSTOLIC = "systolic";
    public static final String COLUMN_EXAM_DIASTOLIC = "diastolic";
    public static final String COLUMN_EXAM_PULSE = "pulse";
    public static final String COLUMN_EXAM_NOTES = "notes";
    public static final String COLUMN_EXAM_CATEGORY = "category";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL," + COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL)";
    private static final String CREATE_TABLE_PROFILES = "CREATE TABLE " + TABLE_PROFILES + "(" + COLUMN_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PROFILE_USER_EMAIL_FK + " TEXT NOT NULL UNIQUE," + COLUMN_PROFILE_NOMBRE_COMPLETO + " TEXT," + COLUMN_PROFILE_EDAD + " INTEGER," + COLUMN_PROFILE_SEXO + " TEXT," + COLUMN_PROFILE_PESO_KG + " REAL," + COLUMN_PROFILE_ALTURA_CM + " INTEGER," + COLUMN_PROFILE_CONDICION_MEDICA + " TEXT," + "FOREIGN KEY(" + COLUMN_PROFILE_USER_EMAIL_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + "))";
    private static final String CREATE_TABLE_CHAT_SESSIONS = "CREATE TABLE " + TABLE_CHAT_SESSIONS + "(" + COLUMN_CHAT_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CHAT_SESSION_USER_EMAIL_FK + " TEXT NOT NULL," + COLUMN_CHAT_SESSION_START_TIMESTAMP + " INTEGER NOT NULL," + COLUMN_CHAT_SESSION_PREVIEW + " TEXT," + "FOREIGN KEY(" + COLUMN_CHAT_SESSION_USER_EMAIL_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + "))";
    private static final String CREATE_TABLE_CHAT_MESSAGES = "CREATE TABLE " + TABLE_CHAT_MESSAGES + "(" + COLUMN_CHAT_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CHAT_MESSAGE_SESSION_ID_FK + " INTEGER NOT NULL," + COLUMN_CHAT_MESSAGE_TEXT + " TEXT NOT NULL," + COLUMN_CHAT_MESSAGE_IS_USER + " INTEGER NOT NULL," + COLUMN_CHAT_MESSAGE_TIMESTAMP + " INTEGER NOT NULL," + "FOREIGN KEY(" + COLUMN_CHAT_MESSAGE_SESSION_ID_FK + ") REFERENCES " + TABLE_CHAT_SESSIONS + "(" + COLUMN_CHAT_SESSION_ID + "))";
    private static final String CREATE_TABLE_EXAMS = "CREATE TABLE " + TABLE_EXAMS + "(" + COLUMN_EXAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_EXAM_USER_EMAIL_FK + " TEXT NOT NULL," + COLUMN_EXAM_TIMESTAMP + " INTEGER NOT NULL," + COLUMN_EXAM_SYSTOLIC + " INTEGER NOT NULL," + COLUMN_EXAM_DIASTOLIC + " INTEGER NOT NULL," + COLUMN_EXAM_PULSE + " INTEGER NOT NULL," + COLUMN_EXAM_NOTES + " TEXT," + COLUMN_EXAM_CATEGORY + " TEXT," + "FOREIGN KEY(" + COLUMN_EXAM_USER_EMAIL_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PROFILES);
        db.execSQL(CREATE_TABLE_CHAT_SESSIONS);
        db.execSQL(CREATE_TABLE_CHAT_MESSAGES);
        db.execSQL(CREATE_TABLE_EXAMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXAMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private String simpleHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return text;
        }
    }

    public boolean addUser(String email, String password) {
        if (checkUserExists(email)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD_HASH, simpleHash(password));
        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_USERS, null, values);
        } finally {
            db.close();
        }
        return result != -1;
    }

    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_EMAIL + " = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                exists = true;
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return exists;
    }

    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_USER_PASSWORD_HASH + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_EMAIL + " = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                int passwordHashColumnIndex = cursor.getColumnIndex(COLUMN_USER_PASSWORD_HASH);
                if(passwordHashColumnIndex != -1) {
                    String storedPasswordHash = cursor.getString(passwordHashColumnIndex);
                    if (simpleHash(password).equals(storedPasswordHash)) {
                        isValid = true;
                    }
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return isValid;
    }

    public boolean addOrUpdateProfile(String userEmail, String nombreCompleto, int edad, String sexo, double pesoKg, int alturaCm, String condicionMedica) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_USER_EMAIL_FK, userEmail);
        values.put(COLUMN_PROFILE_NOMBRE_COMPLETO, nombreCompleto);
        values.put(COLUMN_PROFILE_EDAD, edad);
        values.put(COLUMN_PROFILE_SEXO, sexo);
        values.put(COLUMN_PROFILE_PESO_KG, pesoKg);
        values.put(COLUMN_PROFILE_ALTURA_CM, alturaCm);
        values.put(COLUMN_PROFILE_CONDICION_MEDICA, condicionMedica);
        long result = -1;
        Cursor cursor = null;
        boolean profileExists = false;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_PROFILE_ID + " FROM " + TABLE_PROFILES + " WHERE " + COLUMN_PROFILE_USER_EMAIL_FK + " = ?", new String[]{userEmail});
            if (cursor != null && cursor.moveToFirst()) profileExists = true;
        } finally { if (cursor != null) cursor.close(); }

        try {
            if (profileExists) {
                result = db.update(TABLE_PROFILES, values, COLUMN_PROFILE_USER_EMAIL_FK + " = ?", new String[]{userEmail});
            } else {
                result = db.insertOrThrow(TABLE_PROFILES, null, values);
            }
        } finally {
            db.close();
        }
        return result != -1;
    }

    public Cursor getUserProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_PROFILES + " WHERE " + COLUMN_PROFILE_USER_EMAIL_FK + " = ?", new String[]{email});
        } finally {}
        return cursor;
    }

    public long startNewChatSession(String userEmail, String firstMessagePreview) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_SESSION_USER_EMAIL_FK, userEmail);
        values.put(COLUMN_CHAT_SESSION_START_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_CHAT_SESSION_PREVIEW, firstMessagePreview);
        long sessionId = -1;
        try {
            sessionId = db.insertOrThrow(TABLE_CHAT_SESSIONS, null, values);
        } finally {
            db.close();
        }
        return sessionId;
    }

    public boolean addChatMessageToSession(long sessionId, String messageText, boolean isUserMessage, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_MESSAGE_SESSION_ID_FK, sessionId);
        values.put(COLUMN_CHAT_MESSAGE_TEXT, messageText);
        values.put(COLUMN_CHAT_MESSAGE_IS_USER, isUserMessage ? 1 : 0);
        values.put(COLUMN_CHAT_MESSAGE_TIMESTAMP, timestamp);
        long messageId = -1;
        try {
            messageId = db.insertOrThrow(TABLE_CHAT_MESSAGES, null, values);
        } finally {
            db.close();
        }
        return messageId != -1;
    }

    public List<ChatSessionSummary> getChatSessionsForUser(String userEmail) {
        List<ChatSessionSummary> sessions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_CHAT_SESSION_ID + ", " + COLUMN_CHAT_SESSION_PREVIEW + ", " + COLUMN_CHAT_SESSION_START_TIMESTAMP + " FROM " + TABLE_CHAT_SESSIONS + " WHERE " + COLUMN_CHAT_SESSION_USER_EMAIL_FK + " = ?" + " ORDER BY " + COLUMN_CHAT_SESSION_START_TIMESTAMP + " DESC", new String[]{userEmail});
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHAT_SESSION_ID));
                    String preview = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAT_SESSION_PREVIEW));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHAT_SESSION_START_TIMESTAMP));
                    sessions.add(new ChatSessionSummary(id, preview, timestamp));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return sessions;
    }

    public List<ChatMessage> getMessagesForSession(long sessionId) {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_CHAT_MESSAGE_TEXT + ", " + COLUMN_CHAT_MESSAGE_IS_USER + ", " + COLUMN_CHAT_MESSAGE_TIMESTAMP + " FROM " + TABLE_CHAT_MESSAGES + " WHERE " + COLUMN_CHAT_MESSAGE_SESSION_ID_FK + " = ?" + " ORDER BY " + COLUMN_CHAT_MESSAGE_TIMESTAMP + " ASC", new String[]{String.valueOf(sessionId)});
            if (cursor.moveToFirst()) {
                do {
                    String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAT_MESSAGE_TEXT));
                    boolean isUser = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHAT_MESSAGE_IS_USER)) == 1;
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHAT_MESSAGE_TIMESTAMP));
                    messages.add(new ChatMessage(text, isUser, timestamp));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return messages;
    }

    public boolean addExamRecord(String userEmail, long timestamp, int systolic, int diastolic, int pulse, String notes, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXAM_USER_EMAIL_FK, userEmail);
        values.put(COLUMN_EXAM_TIMESTAMP, timestamp);
        values.put(COLUMN_EXAM_SYSTOLIC, systolic);
        values.put(COLUMN_EXAM_DIASTOLIC, diastolic);
        values.put(COLUMN_EXAM_PULSE, pulse);
        values.put(COLUMN_EXAM_NOTES, notes);
        values.put(COLUMN_EXAM_CATEGORY, category);

        long examId = -1;
        try {
            examId = db.insertOrThrow(TABLE_EXAMS, null, values);
        } finally {
            db.close();
        }
        return examId != -1;
    }

    public List<ExamRecord> getExamRecordsForUser(String userEmail) {
        List<ExamRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_EXAM_ID + ", " + COLUMN_EXAM_TIMESTAMP + ", " + COLUMN_EXAM_SYSTOLIC + ", " + COLUMN_EXAM_DIASTOLIC + ", " + COLUMN_EXAM_PULSE + ", " + COLUMN_EXAM_NOTES + ", " + COLUMN_EXAM_CATEGORY + " FROM " + TABLE_EXAMS + " WHERE " + COLUMN_EXAM_USER_EMAIL_FK + " = ?" + " ORDER BY " + COLUMN_EXAM_TIMESTAMP + " DESC", new String[]{userEmail});
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXAM_ID));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXAM_TIMESTAMP));
                    int systolic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXAM_SYSTOLIC));
                    int diastolic = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXAM_DIASTOLIC));
                    int pulse = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXAM_PULSE));
                    String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAM_NOTES));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAM_CATEGORY));
                    records.add(new ExamRecord(id, userEmail, timestamp, systolic, diastolic, pulse, notes, category));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return records;
    }
}
