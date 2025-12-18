package com.example.swiftserve_admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PollingBaseActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int POLLING_INTERVAL = 10000; // 10 seconds

    private static final String POLLING_PREFS = "PollingState";
    private static final String COUNT_KEY = "last_reservation_count";
    private static final String LAST_UPDATE_KEY = "last_edit_timestamp";

    protected ReservationDatabaseHelper resDb;

    private final Runnable reservationChecker = new Runnable() {
        @Override
        public void run() {
            // 1. Check if the Admin even wants notifications
            SharedPreferences notificationPrefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            boolean masterEnabled = notificationPrefs.getBoolean("notif_enabled", true);

            // If Master is OFF, don't even check the DB. Just wait and try again in 10s.
            if (!masterEnabled) {
                handler.postDelayed(this, POLLING_INTERVAL);
                return;
            }

            // 2. Master is ON, so let's check what specifically they want to see
            boolean incomingEnabled = notificationPrefs.getBoolean("notif_incoming_reservations", false);
            boolean editsEnabled = notificationPrefs.getBoolean("notif_reservation_edits", false);

            SharedPreferences pollingPrefs = getSharedPreferences(POLLING_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = pollingPrefs.edit();

            // 3. Logic for New Reservations
            if (incomingEnabled) {
                int currentCount = resDb.countAll();
                int lastKnownCount = pollingPrefs.getInt(COUNT_KEY, -1);

                if (lastKnownCount != -1 && currentCount > lastKnownCount) {
                    NotificationHelper.show(PollingBaseActivity.this, "New Booking", "A new reservation has arrived!");
                }
                editor.putInt(COUNT_KEY, currentCount);
            }

            // 4. Logic for Guest Edits
            if (editsEnabled) {
                String currentLastEdit = resDb.getLatestEditTimestamp();
                String lastKnownEdit = pollingPrefs.getString(LAST_UPDATE_KEY, "");

                // Only notify if we have a previous timestamp to compare against
                if (!lastKnownEdit.isEmpty() && !currentLastEdit.equals(lastKnownEdit)) {
                    NotificationHelper.show(PollingBaseActivity.this, "Reservation Edited", "A guest updated their booking details.");
                }
                editor.putString(LAST_UPDATE_KEY, currentLastEdit);
            }

            editor.apply(); // Save all changes once
            handler.postDelayed(this, POLLING_INTERVAL);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resDb = new ReservationDatabaseHelper(this);

        // 1. Ask for Permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        NotificationHelper.createChannel(this);
        initializePollingState();

    }

    private void initializePollingState() {
        SharedPreferences pollingPrefs = getSharedPreferences(POLLING_PREFS, MODE_PRIVATE);
        if (!pollingPrefs.contains(COUNT_KEY)) {
            pollingPrefs.edit()
                    .putInt(COUNT_KEY, resDb.countAll())
                    .putString(LAST_UPDATE_KEY, resDb.getLatestEditTimestamp())
                    .apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(reservationChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(reservationChecker);
    }
}