package com.example.swiftserve_admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// This activity will handle polling for new reservations
public class PollingBaseActivity extends AppCompatActivity {

    // Same polling variables and constants from Dashboard
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int POLLING_INTERVAL = 10000; // 10 seconds
    private static final String POLLING_PREFS = "PollingState";
    private static final String COUNT_KEY = "last_reservation_count";

    // You must initialize your database helper here or ensure it's accessible.
    // We'll initialize it in onCreate and assume all extending classes have access.
    protected ReservationDatabaseHelper resDb;

    // The Polling Logic - EXACTLY the same as before
    private final Runnable reservationChecker = new Runnable() {
        @Override
        public void run() {
            // A. Check Notification Settings
            SharedPreferences notificationPrefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            boolean masterEnabled = notificationPrefs.getBoolean("notif_enabled", true);
            boolean incomingEnabled = notificationPrefs.getBoolean("notif_incoming_reservations", false);

            if (masterEnabled && incomingEnabled) {
                // 1. Get current reservation count from the DB
                int currentCount = resDb.countAll();

                // 2. Get the last known count from SharedPreferences
                SharedPreferences pollingPrefs = getSharedPreferences(POLLING_PREFS, MODE_PRIVATE);
                int lastKnownCount = pollingPrefs.getInt(COUNT_KEY, -1);

                if (lastKnownCount != -1 && currentCount > lastKnownCount) {
                    // New reservation detected! Show notification.
                    NotificationHelper.show(
                            PollingBaseActivity.this, // Use PollingBaseActivity.this as context
                            "New Reservation Alert!",
                            "A new reservation has been placed by a guest."
                    );
                }

                // 3. Always update the last known count for the next check
                pollingPrefs.edit().putInt(COUNT_KEY, currentCount).apply();
            }

            // 4. Schedule the next check
            handler.postDelayed(this, POLLING_INTERVAL);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Database Helper
        resDb = new ReservationDatabaseHelper(this);

        // CRUCIAL: Initialize the Polling State on first run
        SharedPreferences pollingPrefs = getSharedPreferences(POLLING_PREFS, MODE_PRIVATE);
        if (!pollingPrefs.contains(COUNT_KEY)) {
            // If the app is launched for the first time, save the current total count
            pollingPrefs.edit().putInt(COUNT_KEY, resDb.countAll()).apply();
        }
    }

    // START/STOP POLLING
    @Override
    protected void onResume() {
        super.onResume();
        // Start the polling loop when the activity comes to the foreground
        handler.post(reservationChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the polling loop when the activity goes to the background
        handler.removeCallbacks(reservationChecker);
    }
}