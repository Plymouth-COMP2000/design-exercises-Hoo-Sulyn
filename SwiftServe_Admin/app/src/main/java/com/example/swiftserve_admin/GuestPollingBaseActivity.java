package com.example.swiftserve_admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GuestPollingBaseActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    protected ReservationDatabaseHelper resDb;
    protected MenuDatabaseHelper menuDb;
    protected String currentGuestEmail;

    private final Runnable guestChecker = new Runnable() {
        @Override
        public void run() {
            SharedPreferences settings = getSharedPreferences("GuestNotificationPrefs", MODE_PRIVATE);
            SharedPreferences state = getSharedPreferences("GuestState", MODE_PRIVATE);
            if (!settings.getBoolean("guest_notif_enabled", true)) {
                handler.postDelayed(this, 10000);
                return;
            }

            // PATTERN: Check Menu Count (Simplest test)
            if (settings.getBoolean("guest_menu_updates", false)) {
                int currentCount = menuDb.getMenuCount();
                int lastCount = state.getInt("last_menu_count", currentCount);

                if (currentCount > lastCount) {
                    NotificationHelper.show(GuestPollingBaseActivity.this, "Menu Update", "New items added!");
                }
                state.edit().putInt("last_menu_count", currentCount).apply();
            }

            if (settings.getBoolean("guest_res_updates", false)) {
                String currentStatus = resDb.getReservationStatus(currentGuestEmail);
                String lastStatus = state.getString("last_known_status", "");

                // Debug Log to see what's happening in Logcat
                android.util.Log.d("POLLING_DEBUG", "Email: " + currentGuestEmail + " | Status: " + currentStatus);

                if (!lastStatus.isEmpty() && currentStatus != null && !currentStatus.equals(lastStatus)) {
                    NotificationHelper.show(GuestPollingBaseActivity.this, "Reservation Update", "Your status is now: " + currentStatus);
                }
                state.edit().putString("last_known_status", currentStatus).apply();
            }

            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resDb = new ReservationDatabaseHelper(this);
        menuDb = new MenuDatabaseHelper(this);

        SharedPreferences userSession = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentGuestEmail = userSession.getString("user_email", "");

        // Use the EXACT same permission logic from Admin
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        NotificationHelper.createChannel(this);
    }

    @Override
    protected void onResume() { super.onResume(); handler.post(guestChecker); }
    @Override
    protected void onPause() { super.onPause(); handler.removeCallbacks(guestChecker); }
}