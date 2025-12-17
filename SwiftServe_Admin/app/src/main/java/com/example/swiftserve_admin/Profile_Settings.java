package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;

public class Profile_Settings extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;

    // Add these variables
    private TextView adminName, adminEmail;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_settings);

        userService = new UserService(this);

        // =======================
        // HEADER
        // =======================
        drawerLayout = findViewById(R.id.main);
        menuButton = findViewById(R.id.side_nav_button);
        closeButton = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));

        // =======================
        // USER PROFILE DATA
        // =======================
        adminName = findViewById(R.id.admin_name);
        adminEmail = findViewById(R.id.admin_email);

        // Load user data
        loadUserData();

        // -----------------------
        // SIDEBAR BUTTONS
        // -----------------------
        View dashboardNav = findViewById(R.id.dashboardButton);
        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        dashboardNav.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Notification_Settings.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());

        // -----------------------
        // SETTINGS OPTIONS
        // -----------------------
        LinearLayout notificationSettings = findViewById(R.id.notification_settings);
        LinearLayout editProfile = findViewById(R.id.edit_profile);

        notificationSettings.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Notification_Settings.class)));
        editProfile.setOnClickListener(v -> startActivity(new Intent(Profile_Settings.this, Edit_Profile.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when coming back from Edit_Profile
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String studentId = "bsse2509244";
        String userId = prefs.getString("logged_in_user_id", "");

        if (userId.isEmpty()) {
            return;
        }

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                // Update name and email on the screen
                String fullName = user.getFirstname() + " " + user.getLastname();
                adminName.setText(fullName);
                adminEmail.setText(user.getEmail());

                // Update sidebar name
                updateSidebarName(user.getFirstname());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(Profile_Settings.this,
                        "Could not load profile: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSidebarName(String firstName) {
        View sideNavView = findViewById(R.id.profile_settings_nav);
        TextView sidebarName = sideNavView.findViewById(R.id.admin_name);
        if (sidebarName != null) {
            sidebarName.setText("Hello, " + firstName);
        }
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Profile_Settings.this);
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

        confirmBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.clear(); // Clear all saved data
            editor.apply();

            Intent intent = new Intent(Profile_Settings.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}