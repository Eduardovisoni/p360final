package com.example.presion360.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.presion360.R;
import com.example.presion360.ui.chat.ChatIAFragment;
import com.example.presion360.ui.exam.ExamsFragment;
import com.example.presion360.ui.history.HistoryFragment;
import com.example.presion360.ui.settings.SettingsFragment;
import com.example.presion360.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationActivity extends AppCompatActivity {

    private static final String TAG = "MainNavigationAct";
    private BottomNavigationView bottomNavigationView;

    private final ExamsFragment examsFragment = new ExamsFragment();
    private final ChatIAFragment chatIAFragment = new ChatIAFragment();
    private final HistoryFragment historyFragment = new HistoryFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    private Fragment activeFragment = null;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        try {
            SharedPreferences directPrefs = getApplicationContext().getSharedPreferences("Presion360UserSession", Context.MODE_PRIVATE);
            String emailFromPrefs = directPrefs.getString("userEmail", "FALLBACK_MAIN_NAV_EMPTY");
            Log.d(TAG, "onCreate - Email leído DIRECTAMENTE de SharedPreferences en MainNavigationActivity: '" + emailFromPrefs + "'");

            String emailFromSessionManager = SessionManager.getInstance(getApplicationContext()).getCurrentUserEmail();
            Log.d(TAG, "onCreate - Email leído via SessionManager en MainNavigationActivity: '" + emailFromSessionManager + "'");

        } catch (Exception e) {
            Log.e(TAG, "onCreate - Error al leer SharedPreferences en MainNavigationActivity: ", e);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container, settingsFragment, "4").hide(settingsFragment);
            transaction.add(R.id.fragment_container, historyFragment, "3").hide(historyFragment);
            transaction.add(R.id.fragment_container, chatIAFragment, "2").hide(chatIAFragment);
            transaction.add(R.id.fragment_container, examsFragment, "1");
            transaction.commitNow();
            activeFragment = examsFragment;
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_exams) {
                selectedFragment = examsFragment;
            } else if (itemId == R.id.navigation_chat_ia) {
                selectedFragment = chatIAFragment;
            } else if (itemId == R.id.navigation_history) {
                selectedFragment = historyFragment;
            } else if (itemId == R.id.navigation_settings) {
                selectedFragment = settingsFragment;
            }

            if (selectedFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (activeFragment != null && activeFragment != selectedFragment) {
                    transaction.hide(activeFragment);
                }
                transaction.show(selectedFragment);
                transaction.commit();
                activeFragment = selectedFragment;
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_exams);
        } else {
            int selectedItemId = bottomNavigationView.getSelectedItemId();
            if (selectedItemId == 0 || fragmentManager.findFragmentById(R.id.fragment_container) == null) {
                 bottomNavigationView.setSelectedItemId(R.id.navigation_exams);
            }
        }
    }
}
