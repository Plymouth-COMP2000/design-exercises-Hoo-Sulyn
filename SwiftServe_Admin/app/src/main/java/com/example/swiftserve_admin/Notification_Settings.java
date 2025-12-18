package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;


public class Notification_Settings extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;

    private CheckBox enableNotifications;
    private CheckBox reservationEdits;
    private CheckBox incomingReservations;

    private SharedPreferences notificationPrefs;

    // Keys used for SharedPreferences
    private static final String KEY_ENABLED = "notif_enabled";
    private static final String KEY_EDITS = "notif_reservation_edits";
    private static final String KEY_INCOMING = "notif_incoming_reservations";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_settings);

        // =======================
        // INITIALIZATION
        // =======================
        enableNotifications = findViewById(R.id.enable_notifications);
        reservationEdits = findViewById(R.id.reservation_edits);
        incomingReservations = findViewById(R.id.incoming_reservations);

        // SharedPreferences
        notificationPrefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        // =======================
        // LOAD SAVED VALUES
        // =======================
        loadSettings();

        // =======================
        // SETUP LISTENERS (FIXED)
        // =======================
        setupListeners();


        // =======================
        // NAVIGATION SETUP
        // =======================
        setupNavigation();
    }

    private void loadSettings() {
        // Load saved values, defaulting to enabled=true, others=false
        boolean masterEnabled = notificationPrefs.getBoolean(KEY_ENABLED, true);

        enableNotifications.setChecked(masterEnabled);
        reservationEdits.setChecked(notificationPrefs.getBoolean(KEY_EDITS, false));
        incomingReservations.setChecked(notificationPrefs.getBoolean(KEY_INCOMING, false));

        // Immediately enforce the child checkbox state based on the master switch
        setChildCheckboxState(masterEnabled);
    }

    private void savePreference(String key, boolean value) {
        notificationPrefs.edit().putBoolean(key, value).apply();
    }

    private void setChildCheckboxState(boolean enabled) {
        // Greys out the text and makes them unclickable
        reservationEdits.setEnabled(enabled);
        incomingReservations.setEnabled(enabled);

        // Set transparency for visual "grey out" effect if disabled
        float alpha = enabled ? 1.0f : 0.4f;
        reservationEdits.setAlpha(alpha);
        incomingReservations.setAlpha(alpha);

        if (!enabled) {
            // If master is disabled, we turn these off visually
            reservationEdits.setChecked(false);
            incomingReservations.setChecked(false);
        }
    }

    private void setupListeners() {
        // Listener for the master 'Enable Notifications' switch
        enableNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_ENABLED, isChecked);
            setChildCheckboxState(isChecked); // Call the helper to manage child switches
            Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        // Listener for 'Receive Reservation Edits'
        reservationEdits.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only save if the master switch is currently enabled
            if (enableNotifications.isChecked()) {
                savePreference(KEY_EDITS, isChecked);
                Toast.makeText(this, "Reservation Edits Alert " + (isChecked ? "On" : "Off"), Toast.LENGTH_SHORT).show();
            }
        });

        // Listener for 'Receive Incoming Reservations'
        incomingReservations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only save if the master switch is currently enabled
            if (enableNotifications.isChecked()) {
                savePreference(KEY_INCOMING, isChecked);
                Toast.makeText(this, "Incoming Reservations Alert " + (isChecked ? "On" : "Off"), Toast.LENGTH_SHORT).show();
            }
        });

        // BACK BUTTON
        TextView back_button = findViewById(R.id.back_button);
        // Changed destination to Dashboard as Profile_Settings might not be the desired back destination
        back_button.setOnClickListener(v -> finish());
    }

    private void setupNavigation() {
        // HEADER
        drawerLayout = findViewById(R.id.main);
        menuButton = findViewById(R.id.side_nav_button);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));

        // SIDEBAR BUTTONS
        View sideNavView = findViewById(R.id.menu_management_nav); // Assuming this ID is correct from your XML
        ImageView closeButton = sideNavView.findViewById(R.id.closeSideNav);
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Navigation links (Found inside the sideNavView)
        View dashboardNav = sideNavView.findViewById(R.id.dashboardButton);
        View menuNav = sideNavView.findViewById(R.id.menuButton);
        View reservationNav = sideNavView.findViewById(R.id.reservationButton);
        View profileNav = sideNavView.findViewById(R.id.profileButton);
        View notificationSettingsNav = sideNavView.findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);

        dashboardNav.setOnClickListener(v -> startActivity(new Intent(this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START)); // Close drawer since we are here

        logoutNav.setOnClickListener(v -> showLogoutDialog());
    }


    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Notification_Settings.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_logout_confirmation_popup);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        MaterialButton confirmBtn = dialog.findViewById(R.id.confirmCancel);
        MaterialButton cancelBtn  = dialog.findViewById(R.id.noCancel);

        // CLEAR LOGIN SESSION (Using the fixed, explicit remove logic)
        confirmBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.remove("is_logged_in");
            editor.apply();

            Intent intent = new Intent(Notification_Settings.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}