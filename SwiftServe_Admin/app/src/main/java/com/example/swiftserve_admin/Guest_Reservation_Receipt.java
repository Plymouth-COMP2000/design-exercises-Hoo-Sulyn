package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class Guest_Reservation_Receipt extends GuestPollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;
    private long reservationId = -1;

    // Greeting logic variables
    private UserService userService;
    private String studentId = "bsse2509244";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_reservation_receipt);

        userService = new UserService(this);

        // 1. Initialize Header & Sidebar
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));

        // 2. Setup Sidebar Buttons (Using your specific include ID: reservation_receipt_nav)
        View sideNavView = findViewById(R.id.reservation_receipt_nav);
        if (sideNavView != null) {
            View menuNav = sideNavView.findViewById(R.id.menuButton);
            View reservationNav = sideNavView.findViewById(R.id.reservationButton);
            View historyNav = sideNavView.findViewById(R.id.reservationHistoryButton);
            View profileNav = sideNavView.findViewById(R.id.profileButton);
            View notificationNav = sideNavView.findViewById(R.id.notificationSettingsButton);
            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            ImageView closeSideBtn = sideNavView.findViewById(R.id.closeSideNav);

            if (menuNav != null) menuNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            if (reservationNav != null) reservationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            if (historyNav != null) historyNav.setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
            if (profileNav != null) profileNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
            if (notificationNav != null) notificationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Notification_Settings_Page.class)));
            if (closeSideBtn != null) closeSideBtn.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (logoutNav != null) logoutNav.setOnClickListener(v -> showLogoutDialog());
        }

        // 3. Footer Navigation
        findViewById(R.id.footer_menu).setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
        findViewById(R.id.footer_reservation).setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
        findViewById(R.id.footer_history).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));

        // 4. Back Button & Edit Details
        TextView backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());

        LinearLayout editDetailsLayout = findViewById(R.id.edit_details);
        editDetailsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, Guest_Edit_Reservation.class);
            intent.putExtra("RESERVATION_ID", reservationId);
            startActivity(intent);
        });

        // 5. Load Database Data
        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1);
        loadReservationDetails();

        // 6. Cancel Button Logic
        MaterialButton cancelReservationBtn = findViewById(R.id.cancel_reservation_button);
        cancelReservationBtn.setOnClickListener(v -> showCancellationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Fetch name for "Hello, Name!" greeting
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("logged_in_user_id", "");
        if (userId.isEmpty()) return;

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                View sideNavView = findViewById(R.id.reservation_receipt_nav);
                if (sideNavView != null) {
                    TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);
                    if (greeting != null) greeting.setText("Hello, " + user.getFirstname() + "!");
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void loadReservationDetails() {
        if (reservationId == -1) return;

        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        Reservation res = db.getReservationById(reservationId);

        if (res != null) {
            TextView tvId = findViewById(R.id.reservation_id);
            TextView tvStatus = findViewById(R.id.status);
            TextView tvName = findViewById(R.id.guest_name);
            TextView tvEmail = findViewById(R.id.guest_email);
            TextView tvPhone = findViewById(R.id.guest_phone_num);
            TextView tvDate = findViewById(R.id.reserve_date);
            TextView tvTime = findViewById(R.id.reserve_time);
            TextView tvTable = findViewById(R.id.table_num);
            TextView tvGuests = findViewById(R.id.total_guests);
            TextView tvComment = findViewById(R.id.add_comment);
            LinearLayout declinedLayout = findViewById(R.id.declined_reason_layout);
            TextView tvDeclinedReason = findViewById(R.id.declined_reason_text);
            LinearLayout editDetailsLayout = findViewById(R.id.edit_details);
            MaterialButton cancelBtn = findViewById(R.id.cancel_reservation_button);

            tvId.setText("# " + res.getId());
            tvStatus.setText(res.getStatus());
            tvName.setText(res.getGuestName());
            tvEmail.setText(res.getEmail());
            tvPhone.setText(res.getPhone());
            tvDate.setText(res.getDate());
            tvTime.setText(res.getTime());
            tvTable.setText(res.getTableNum());
            tvGuests.setText(String.valueOf(res.getGuests()));
            tvComment.setText(res.getDetails());

            // Status UI Logic
            String status = res.getStatus();
            if (status.equalsIgnoreCase("Pending")) {
                tvStatus.setTextColor(Color.parseColor("#FF9000"));
                editDetailsLayout.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.VISIBLE);
                declinedLayout.setVisibility(View.GONE);
            } else if (status.equalsIgnoreCase("Declined")) {
                tvStatus.setTextColor(Color.RED);
                editDetailsLayout.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                declinedLayout.setVisibility(View.VISIBLE);
                tvDeclinedReason.setText(res.getReason() != null ? res.getReason() : "No reason provided.");
            } else {
                editDetailsLayout.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                declinedLayout.setVisibility(View.GONE);
                tvStatus.setTextColor(status.equalsIgnoreCase("Accepted") ? Color.parseColor("#2EC23D") : Color.GRAY);
            }
        }
    }

    private void showCancellationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.guest_reservation_cancellation_popup);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.findViewById(R.id.confirmCancel).setOnClickListener(v -> {
            new ReservationDatabaseHelper(this).deleteReservation(reservationId);
            Toast.makeText(this, "Reservation Cancelled", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Reservation_History.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_logout_confirmation_popup);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.findViewById(R.id.confirmCancel).setOnClickListener(v -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}