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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Edit_Profile extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;

    private TextInputEditText editFirstName, editLastName, editEmail, editContact;
    private MaterialButton saveButton;

    private String studentId = "bsse2509244";
    private String userId;
    private String originalEmail;
    private String username, password, userType;

    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // GET USER ID FROM LOGIN
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("logged_in_user_id", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        userService = new UserService(this);
        setupViews();
        loadUserData();
    }

    private void setupViews() {
        drawerLayout = findViewById(R.id.main);
        menuButton = findViewById(R.id.side_nav_button);
        profileHeader = findViewById(R.id.profile_nav);
        View dashboardNav = findViewById(R.id.dashboardButton);
        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editEmail = findViewById(R.id.edit_email);
        editContact = findViewById(R.id.edit_phone_num);
        saveButton = findViewById(R.id.save_profile_button);

        saveButton.setOnClickListener(v -> saveProfile());

        // Back button
        TextView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Menu
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));
        dashboardNav.setOnClickListener(v -> startActivity(new Intent(Edit_Profile.this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(Edit_Profile.this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Edit_Profile.this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Edit_Profile.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Edit_Profile.this, Notification_Settings.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                // FILL FORM WITH CURRENT DATA
                editFirstName.setText(user.getFirstname());
                editLastName.setText(user.getLastname());
                editEmail.setText(user.getEmail());
                editContact.setText(user.getContact());

                // SAVE FOR LATER
                originalEmail = user.getEmail();
                username = user.getUsername();
                password = user.getPassword();
                userType = user.getUsertype();

                editEmail.setFocusable(false);
                editEmail.setClickable(false);

                // UPDATE SIDEBAR ON LOAD TOO
                updateSidebarName(user.getFirstname());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(Edit_Profile.this, "Error loading: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String contact = editContact.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.equals(originalEmail)) {
            Toast.makeText(this, "Cannot change email", Toast.LENGTH_LONG).show();
            return;
        }

        User updatedUser = new User(username, password, firstName, lastName, email, contact, userType);

        userService.updateUserProfile(studentId, userId, updatedUser,
                new UserService.UserProfileListener() {
                    @Override
                    public void onSuccess(User user) {
                        Toast.makeText(Edit_Profile.this, "Profile updated", Toast.LENGTH_SHORT).show();

                        // UPDATE SIDEBAR NAME
                        updateSidebarName(user.getFirstname());

                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(Edit_Profile.this, "Update failed: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateSidebarName(String firstName) {
        View sideNavView = findViewById(R.id.edit_profile_nav);
        TextView helloNameTextView = sideNavView.findViewById(R.id.admin_name);

        if (helloNameTextView != null) {
            helloNameTextView.setText("Hello, " + firstName);
        }
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Edit_Profile.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_logout_confirmation_popup);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        MaterialButton confirmBtn = dialog.findViewById(R.id.confirmCancel);
        MaterialButton cancelBtn = dialog.findViewById(R.id.noCancel);

        confirmBtn.setOnClickListener(view -> {
            // CLEAR LOGIN SESSION
            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            Intent intent = new Intent(Edit_Profile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
}