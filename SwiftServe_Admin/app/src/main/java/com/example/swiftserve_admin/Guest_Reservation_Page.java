package com.example.swiftserve_admin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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

import java.util.Calendar;
import java.util.Locale;

public class Guest_Reservation_Page extends GuestPollingBaseActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;
    private UserService userService;
    private String studentId = "bsse2509244";

    // Input Fields
    private TextInputEditText dateEdit, timeEdit, guestsEdit, firstNameEdit, lastNameEdit, emailEdit, phoneEdit, detailsEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_reservation_page);

        userService = new UserService(this);

        initHeaderAndSidebar();
        initFormFields();
        initFooter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Refresh profile info and sidebar greeting
    }

    private void initFormFields() {
        dateEdit = findViewById(R.id.date);
        timeEdit = findViewById(R.id.time);
        guestsEdit = findViewById(R.id.num_guests);
        firstNameEdit = findViewById(R.id.first_name);
        lastNameEdit = findViewById(R.id.last_name);
        emailEdit = findViewById(R.id.email);
        phoneEdit = findViewById(R.id.phone_num);
        detailsEdit = findViewById(R.id.add_details);

        if (dateEdit != null) {
            dateEdit.setFocusable(false);
            dateEdit.setOnClickListener(v -> showDatePicker());
        }

        if (timeEdit != null) {
            timeEdit.setFocusable(false);
            timeEdit.setOnClickListener(v -> showTimePicker());
        }

        MaterialButton viewSummary = findViewById(R.id.view_summary_button);
        if (viewSummary != null) {
            viewSummary.setOnClickListener(v -> proceedToSummary());
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("logged_in_user_id", "");

        if (userId.isEmpty()) return;

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                // 1. Update the sidebar greeting
                updateSidebarGreeting(user.getFirstname());

                // 2. AUTO-FILL THE FORM FIELDS (Fixed this part!)
                if (firstNameEdit != null) firstNameEdit.setText(user.getFirstname());
                if (lastNameEdit != null) lastNameEdit.setText(user.getLastname());
                if (emailEdit != null) emailEdit.setText(user.getEmail());
                if (phoneEdit != null) phoneEdit.setText(user.getContact());
            }

            @Override
            public void onError(String message) {
                // Fail silently or show a small toast
            }
        });
    }

    private void updateSidebarGreeting(String firstName) {
        // 1. Find the sidebar container (the <include> tag ID from your main XML)
        View sideNavView = findViewById(R.id.reservation_side_nav);

        if (sideNavView != null) {
            // 2. Find the TextView inside your RelativeLayout sidebar
            TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);

            if (greeting != null) {
                // 3. Set the text
                greeting.setText("Hello, " + firstName + "!");
            }
        }
    }

    private void initHeaderAndSidebar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        profileHeader = findViewById(R.id.profile_nav);

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (profileHeader != null) {
            profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
        }

        // Setup Sidebar Buttons via Include
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
            if (reservationNav != null) reservationNav.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (historyNav != null) historyNav.setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
            if (profileNav != null) profileNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
            if (closeBtn != null) closeBtn.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (logoutNav != null) logoutNav.setOnClickListener(v -> showLogoutDialog());
        }
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
            finish();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) -> {
            dateEdit.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    private void showTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
            timeEdit.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        }, 18, 0, true);
        picker.show();
    }

    private void proceedToSummary() {
        String fullName = firstNameEdit.getText().toString().trim() + " " + lastNameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();
        String date = dateEdit.getText().toString().trim();
        String time = timeEdit.getText().toString().trim();
        String guests = guestsEdit.getText().toString().trim();
        String details = detailsEdit.getText().toString().trim();

        if (fullName.isEmpty() || date.isEmpty() || guests.isEmpty()) {
            Toast.makeText(this, "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, Guest_Reservation_Summary.class);
        intent.putExtra("GUEST_NAME", fullName);
        intent.putExtra("EMAIL", email);
        intent.putExtra("PHONE", phone);
        intent.putExtra("DATE", date);
        intent.putExtra("TIME", time);
        intent.putExtra("GUESTS", guests);
        intent.putExtra("DETAILS", details);
        startActivity(intent);
    }

    private void initFooter() {
        findViewById(R.id.footer_menu).setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
        findViewById(R.id.footer_history).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
    }
}