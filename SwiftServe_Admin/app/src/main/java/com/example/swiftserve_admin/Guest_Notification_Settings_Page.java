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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;

public class Guest_Notification_Settings_Page extends GuestPollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;

    private UserService userService;
    private String studentId = "bsse2509244";
    private CheckBox checkEnable, checkMenu, checkRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_notification_settings_page);

        userService = new UserService(this);

        // 1. Setup Header and Sidebar
        setupNavigation();

        // 2. Setup Back Button (from the main activity layout)
        TextView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        setupNotificationSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserGreeting(); // Refreshes the "Hello, [Name]" in sidebar
    }

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView); // Top toolbar menu icon
        profileHeader = findViewById(R.id.profile_nav); // Top toolbar profile icon

        if (menuButton != null) menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (profileHeader != null) profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));

        // --- SIDEBAR (Finding IDs inside the notification_settings_nav include) ---
        View sideNavView = findViewById(R.id.notification_settings_nav);
        if (sideNavView != null) {
            // Navigation Buttons
            sideNavView.findViewById(R.id.menuButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            sideNavView.findViewById(R.id.reservationButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            sideNavView.findViewById(R.id.reservationHistoryButton).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
            sideNavView.findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));

            // Current Page (Just close drawer)
            sideNavView.findViewById(R.id.notificationSettingsButton).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

            // Close Sidebar Icon
            sideNavView.findViewById(R.id.closeSideNav).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

            // Logout
            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            if (logoutNav != null) logoutNav.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void loadUserGreeting() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("logged_in_user_id", "");
        if (userId.isEmpty()) return;

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                View sideNavView = findViewById(R.id.notification_settings_nav);
                if (sideNavView != null) {
                    TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);
                    if (greeting != null) {
                        greeting.setText("Hello, " + user.getFirstname() + "!");
                    }
                }
            }
            @Override
            public void onError(String message) { /* Optional: handle error */ }
        });
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_logout_confirmation_popup);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        dialog.findViewById(R.id.confirmCancel).setOnClickListener(v -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();
        });

        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupNotificationSettings() {
        checkEnable = findViewById(R.id.check_enable_notifications);
        checkMenu = findViewById(R.id.check_menu_updates);
        checkRes = findViewById(R.id.check_res_updates);

        SharedPreferences settings = getSharedPreferences("GuestNotificationPrefs", MODE_PRIVATE);

        // 1. LOAD existing states (default to false or true as needed)
        checkEnable.setChecked(settings.getBoolean("guest_notif_enabled", true));
        checkMenu.setChecked(settings.getBoolean("guest_menu_updates", false));
        checkRes.setChecked(settings.getBoolean("guest_res_updates", false));

        // 2. SAVE changes automatically (Auto-Save)
        checkEnable.setOnCheckedChangeListener((v, isChecked) -> {
            settings.edit().putBoolean("guest_notif_enabled", isChecked).apply();
        });

        checkMenu.setOnCheckedChangeListener((v, isChecked) -> {
            settings.edit().putBoolean("guest_menu_updates", isChecked).apply();
        });

        checkRes.setOnCheckedChangeListener((v, isChecked) -> {
            settings.edit().putBoolean("guest_res_updates", isChecked).apply();
        });
    }
}