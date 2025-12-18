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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;

public class Guest_Profile_Settings extends GuestPollingBaseActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;
    private TextView guestName, guestEmail;
    private UserService userService;
    private MaterialButton btnDeleteAccount;
    private String studentId = "bsse2509244";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_profile_settings);

        // 1. Initialize Service and Main Page Views
        userService = new UserService(this);
        guestName = findViewById(R.id.guest_name);
        guestEmail = findViewById(R.id.guest_email);
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.side_nav_button);
        profileHeader = findViewById(R.id.profile_nav);

        // 2. Setup Sidebar (Navigation Drawer)
        setupNavigationDrawer();

        // 3. Setup Main Profile Buttons (The rows in your ScrollView)
        setupSettingsButtons();

        // 4. Header Click Listeners
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> {
            // Already on profile page, so just close the drawer if open
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("logged_in_user_id", ""); // Or username if that's your ID

        btnDeleteAccount = findViewById(R.id.delete_account);
        btnDeleteAccount.setOnClickListener(v -> {
            // Basic Alert Dialog for a "Serious" feel
            new android.app.AlertDialog.Builder(this)
                    .setTitle("WARNING: Delete Account")
                    .setMessage("This action is permanent. All your profile data and reservations will be deleted. Do you want to proceed?")
                    .setPositiveButton("DELETE ANYWAY", (dialog, which) -> {
                        executeDeleteRequest();
                    })
                    .setNegativeButton("CANCEL", null)
                    .setIcon(android.R.drawable.ic_dialog_alert) // Adds the warning triangle icon
                    .setCancelable(false) // Forces them to click a button
                    .show();
        });
    }

    private void setupNavigationDrawer() {
        // Since the sidebar is an <include>, we find the container first
        View sideNavView = findViewById(R.id.guest_profile_settings_nav);

        if (sideNavView != null) {
            View menuNav = sideNavView.findViewById(R.id.menuButton);
            View reservationNav = sideNavView.findViewById(R.id.reservationButton);
            View historyNav = sideNavView.findViewById(R.id.reservationHistoryButton);
            View profileNav = sideNavView.findViewById(R.id.profileButton);
            View notificationNav = sideNavView.findViewById(R.id.notificationSettingsButton);
            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            ImageView closeButton = sideNavView.findViewById(R.id.closeSideNav);

            // Set individual listeners with null safety
            if (menuNav != null) menuNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            if (reservationNav != null) reservationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            if (historyNav != null) historyNav.setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
            if (profileNav != null) profileNav.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (notificationNav != null) notificationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Notification_Settings_Page.class)));
            if (closeButton != null) closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (logoutNav != null) logoutNav.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void setupSettingsButtons() {
        // Matching the IDs exactly from your activity_guest_profile_settings.xml
        LinearLayout notificationSettings = findViewById(R.id.notification_settings_button);
        LinearLayout editProfile = findViewById(R.id.edit_profile_button);

        if (notificationSettings != null) {
            notificationSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, Guest_Notification_Settings_Page.class)));
        }

        if (editProfile != null) {
            editProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, Guest_Edit_Profile.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always refresh data when returning to this page
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String studentId = "bsse2509244";
        String userId = prefs.getString("logged_in_user_id", "");

        if (userId.isEmpty()) return;

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                // Update text on main profile screen
                String fullName = user.getFirstname() + " " + user.getLastname();
                if (guestName != null) guestName.setText(fullName);
                if (guestEmail != null) guestEmail.setText(user.getEmail());

                // Update the "Hello, Name" text in the sidebar
                updateSidebarName(user.getFirstname());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(Guest_Profile_Settings.this, "Profile load failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSidebarName(String firstName) {
        // 1. Find the sidebar 'include' container
        View sideNavView = findViewById(R.id.guest_profile_settings_nav);

        if (sideNavView != null) {
            // 2. Find the guest_name TextView INSIDE that container
            TextView sidebarName = sideNavView.findViewById(R.id.guest_greeting_name);

            if (sidebarName != null) {
                // 3. Set the text with the greeting format
                sidebarName.setText("Hello, " + firstName + "!");
            }
        }
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_logout_confirmation_popup);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        MaterialButton confirmBtn = dialog.findViewById(R.id.confirmCancel);
        MaterialButton cancelBtn = dialog.findViewById(R.id.noCancel);

        confirmBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteConfirmation() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure? This action is permanent and your data will be lost.")
                .setPositiveButton("Delete", (dialog, which) -> performDelete())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDelete() {
        userService.deleteUser(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(Guest_Profile_Settings.this, "Account Deleted", Toast.LENGTH_LONG).show();

                // 1. Clear SharedPreferences so they aren't "logged in" to a dead account
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

                // 2. Send them back to the Login Screen
                Intent intent = new Intent(Guest_Profile_Settings.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(Guest_Profile_Settings.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeDeleteRequest() {
        userService.deleteUser(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(Guest_Profile_Settings.this, "Account deleted successfully", Toast.LENGTH_LONG).show();

                // Wipe session data
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

                // Redirect to Login and clear the backstack
                Intent intent = new Intent(Guest_Profile_Settings.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(Guest_Profile_Settings.this, "Failed to delete: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}