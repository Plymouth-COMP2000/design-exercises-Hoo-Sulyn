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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Guest_Edit_Profile extends GuestPollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;
    private TextInputEditText etFirstName, etLastName, etEmail, etPhone;
    private MaterialButton btnSave;
    private UserService userService;
    private String studentId = "bsse2509244";
    private String userId;
    private User currentUser;
    private String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_edit_profile);

        userService = new UserService(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("logged_in_user_id", "");

        // 1. Initialize Views (Matches your XML IDs)
        drawerLayout = findViewById(R.id.main); // ID is 'main' in your XML
        menuButton = findViewById(R.id.side_nav_button);
        profileHeader = findViewById(R.id.profile_nav);
        TextView backButton = findViewById(R.id.back_button);

        etFirstName = findViewById(R.id.edit_first_name);
        etLastName = findViewById(R.id.edit_last_name);
        etEmail = findViewById(R.id.edit_email);
        etPhone = findViewById(R.id.edit_phone_num);
        btnSave = findViewById(R.id.save_profile_button);

        // 2. Setup Navigation
        setupNavigationDrawer();

        if (menuButton != null) menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (profileHeader != null) profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        // 3. Load Current Data & Save Action
        loadCurrentUserData();
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadCurrentUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("logged_in_user_id", "");

        if (userId.isEmpty()) return;

        // Call your existing service to fetch user data
        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;

                etFirstName.setText(user.getFirstname());
                etLastName.setText(user.getLastname());
                etEmail.setText(user.getEmail());
                etPhone.setText(user.getContact());

                originalEmail = user.getEmail();
                etEmail.setFocusable(false);
                etEmail.setClickable(false);
                updateSidebarGreeting(user.getFirstname());
            }


            @Override
            public void onError(String message) {
                Toast.makeText(Guest_Edit_Profile.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSidebarGreeting(String name) {
        View sideNavView = findViewById(R.id.edit_profile_nav);
        if (sideNavView != null) {
            TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);
            if (greeting != null) greeting.setText("Hello, " + name + "!");
        }
    }

    private void setupNavigationDrawer() {
        // Find the include tag ID: edit_profile_nav
        View sideNavView = findViewById(R.id.edit_profile_nav);

        if (sideNavView != null) {
            sideNavView.findViewById(R.id.menuButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            sideNavView.findViewById(R.id.reservationButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            sideNavView.findViewById(R.id.reservationHistoryButton).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
            sideNavView.findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
            sideNavView.findViewById(R.id.notificationSettingsButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Notification_Settings_Page.class)));

            ImageView closeBtn = sideNavView.findViewById(R.id.closeSideNav);
            if (closeBtn != null) closeBtn.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            if (logoutNav != null) logoutNav.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void saveProfileChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "Profile data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.equals(originalEmail)) {
            Toast.makeText(this, "Email cannot be changed", Toast.LENGTH_LONG).show();
            return;
        }

        currentUser.setFirstname(firstName);
        currentUser.setLastname(lastName);
        currentUser.setContact(phone);

        userService.updateUserProfile(studentId, userId, currentUser,
                new UserService.UserProfileListener() {
                    @Override
                    public void onSuccess(User user) {
                        Toast.makeText(Guest_Edit_Profile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(Guest_Edit_Profile.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
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
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finishAffinity();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}