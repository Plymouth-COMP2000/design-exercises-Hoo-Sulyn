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

public class Guest_Reservation_Summary extends GuestPollingBaseActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;

    // Greeting logic variables
    private UserService userService;
    private String studentId = "bsse2509244";

    // Summary TextViews
    private TextView tvName, tvEmail, tvPhone, tvDate, tvTime, tvGuests, tvComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_reservation_summary);

        userService = new UserService(this);

        initViews();
        displayReservationData();
        setupNavigation();

        MaterialButton confirmBtn = findViewById(R.id.confirm_reservation_button);
        if (confirmBtn != null) {
            confirmBtn.setOnClickListener(v -> confirmReservation());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // This triggers the "Hello, Name!" update
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("logged_in_user_id", "");

        if (userId.isEmpty()) return;

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                updateSidebarGreeting(user.getFirstname());
            }

            @Override
            public void onError(String message) {
                // Fail silently
            }
        });
    }

    private void updateSidebarGreeting(String firstName) {
        // Find the include container.
        // Note: Using 'menu_side_nav' to match your Reservation page code.
        View sideNavView = findViewById(R.id.reservation_side_nav);
        if (sideNavView == null) sideNavView = findViewById(R.id.guest_profile_settings_nav);

        if (sideNavView != null) {
            TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);
            if (greeting != null) {
                greeting.setText("Hello, " + firstName + "!");
            }
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.full_name);
        tvEmail = findViewById(R.id.email);
        tvPhone = findViewById(R.id.phone_num);
        tvDate = findViewById(R.id.reserve_date);
        tvTime = findViewById(R.id.reserve_time);
        tvGuests = findViewById(R.id.total_guests);
        tvComment = findViewById(R.id.add_comment);

        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        profileHeader = findViewById(R.id.profile_nav);
    }

    private void displayReservationData() {
        Intent intent = getIntent();
        if (tvName != null) tvName.setText(intent.getStringExtra("GUEST_NAME"));
        if (tvEmail != null) tvEmail.setText(intent.getStringExtra("EMAIL"));
        if (tvPhone != null) tvPhone.setText(intent.getStringExtra("PHONE"));
        if (tvDate != null) tvDate.setText(intent.getStringExtra("DATE"));
        if (tvTime != null) tvTime.setText(intent.getStringExtra("TIME"));
        if (tvGuests != null) tvGuests.setText(intent.getStringExtra("GUESTS"));

        if (tvComment != null) {
            String comment = intent.getStringExtra("DETAILS");
            tvComment.setText((comment == null || comment.isEmpty()) ? "None" : comment);
        }
    }

    private void setupNavigation() {
        // Back Button
        TextView backBtn = findViewById(R.id.back_button);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        // Header Menu Button
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        // Header Profile Icon
        if (profileHeader != null) {
            profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
        }

        // Sidebar Buttons Logic
        View sideNavView = findViewById(R.id.reservation_side_nav);
        if (sideNavView == null) sideNavView = findViewById(R.id.guest_profile_settings_nav);

        if (sideNavView != null) {
            View menuNav = sideNavView.findViewById(R.id.menuButton);
            View reservationNav = sideNavView.findViewById(R.id.reservationButton);
            View historyNav = sideNavView.findViewById(R.id.reservationHistoryButton);
            View profileNav = sideNavView.findViewById(R.id.profileButton);
            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            ImageView closeBtn = sideNavView.findViewById(R.id.closeSideNav);

            if (menuNav != null) menuNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            if (reservationNav != null) reservationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            if (historyNav != null) historyNav.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (profileNav != null) profileNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
            if (closeBtn != null) closeBtn.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (logoutNav != null) logoutNav.setOnClickListener(v -> showLogoutDialog());
        }

        // Footer Logic
        View footerMenu = findViewById(R.id.footer_menu);
        View footerReservation = findViewById(R.id.footer_reservation);
        View footerHistory = findViewById(R.id.footer_history);

        if (footerMenu != null) footerMenu.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
        if (footerReservation != null) footerReservation.setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
        if (footerHistory != null) footerHistory.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
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
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void confirmReservation() {
        Intent intent = getIntent();
        String guestsStr = intent.getStringExtra("GUESTS");
        int guestCount = 1;
        try {
            if (guestsStr != null) guestCount = Integer.parseInt(guestsStr);
        } catch (NumberFormatException e) {
            guestCount = 1;
        }

        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        long newId = db.addReservation(
                intent.getStringExtra("GUEST_NAME"),
                intent.getStringExtra("EMAIL"),
                intent.getStringExtra("PHONE"),
                intent.getStringExtra("DATE"),
                intent.getStringExtra("TIME"),
                guestCount,
                "Pending",
                intent.getStringExtra("DETAILS")
        );

        if (newId != -1) {
            Toast.makeText(this, "Reservation Confirmed!", Toast.LENGTH_SHORT).show();
            Intent nextIntent = new Intent(this, Guest_Confirmation_Page.class);
            nextIntent.putExtra("RESERVATION_ID", newId);
            startActivity(nextIntent);
            finishAffinity();
        } else {
            Toast.makeText(this, "Error saving reservation", Toast.LENGTH_SHORT).show();
        }
    }
}