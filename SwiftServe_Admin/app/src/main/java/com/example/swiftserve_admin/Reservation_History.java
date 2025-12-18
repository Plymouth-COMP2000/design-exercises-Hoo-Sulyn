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

import java.util.List;

public class Reservation_History extends GuestPollingBaseActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;

    // Greeting logic variables
    private UserService userService;
    private String studentId = "bsse2509244";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reservation_history);

        userService = new UserService(this);

        initHeaderAndSidebar();
        initFooter();
        setupClearHistory();

        // Initial load of data
        loadUserHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Updates greeting name and refreshes list
        loadUserHistory();
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
        // Find the include container using the ID from your XML
        View sideNavView = findViewById(R.id.reservation_history_side_nav);

        if (sideNavView != null) {
            TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);
            if (greeting != null) {
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

        // Setup Sidebar Buttons via Include Container
        View sideNavView = findViewById(R.id.reservation_history_side_nav);
        if (sideNavView != null) {
            View menuNav = sideNavView.findViewById(R.id.menuButton);
            View reservationNav = sideNavView.findViewById(R.id.reservationButton);
            View historyNav = sideNavView.findViewById(R.id.reservationHistoryButton);
            View profileNav = sideNavView.findViewById(R.id.profileButton);
            View notificationNav = sideNavView.findViewById(R.id.notificationSettingsButton);
            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            ImageView closeBtn = sideNavView.findViewById(R.id.closeSideNav);

            if (menuNav != null) menuNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            if (reservationNav != null) reservationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            if (historyNav != null) historyNav.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
            if (profileNav != null) profileNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
            if (notificationNav != null) notificationNav.setOnClickListener(v -> startActivity(new Intent(this, Guest_Notification_Settings_Page.class)));
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

    private void loadUserHistory() {
        LinearLayout historyContainer = findViewById(R.id.history_cards);
        if (historyContainer == null) return;

        historyContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");

        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        List<Reservation> list = db.getHistoryByEmail(userEmail);

        if (list.isEmpty()) {
            TextView emptyMsg = new TextView(this);
            emptyMsg.setText("No reservation history found.");
            emptyMsg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            emptyMsg.setPadding(0, 100, 0, 0);
            emptyMsg.setTextSize(18);
            emptyMsg.setTextColor(Color.GRAY);
            historyContainer.addView(emptyMsg);
            return;
        }

        for (Reservation r : list) {
            View card = getLayoutInflater().inflate(R.layout.reservation_history_template, historyContainer, false);

            TextView tvDate = card.findViewById(R.id.date);
            TextView tvTime = card.findViewById(R.id.time);
            TextView tvStatus = card.findViewById(R.id.status);
            TextView tvId = card.findViewById(R.id.reservation_id);
            TextView tvTable = card.findViewById(R.id.table_num);
            TextView btnDetails = card.findViewById(R.id.view_details);

            tvDate.setText(r.getDate());
            tvTime.setText(r.getTime());
            tvId.setText("# " + r.getId());
            tvTable.setText(r.getTableNum());
            tvStatus.setText(r.getStatus());

            if ("Pending".equalsIgnoreCase(r.getStatus())) {
                tvStatus.setTextColor(Color.parseColor("#FF9000"));
            } else if ("Accepted".equalsIgnoreCase(r.getStatus())) {
                tvStatus.setTextColor(Color.parseColor("#2EC23D"));
            } else if ("Declined".equalsIgnoreCase(r.getStatus())) {
                tvStatus.setTextColor(Color.RED);
            } else {
                tvStatus.setTextColor(Color.GRAY);
            }

            btnDetails.setOnClickListener(v -> {
                Intent intent = new Intent(this, Guest_Reservation_Receipt.class);
                intent.putExtra("RESERVATION_ID", (long) r.getId());
                startActivity(intent);
            });

            historyContainer.addView(card);
        }
    }

    private void setupClearHistory() {
        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        TextView clearHistoryBtn = findViewById(R.id.clear_history);

        if (clearHistoryBtn != null) {
            clearHistoryBtn.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Clear History")
                        .setMessage("Are you sure you want to delete all your reservation history?")
                        .setPositiveButton("Yes, Clear", (dialog, which) -> {
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            String userEmail = prefs.getString("user_email", "");
                            db.deleteHistoryByEmail(userEmail);
                            loadUserHistory();
                            Toast.makeText(this, "History cleared!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void initFooter() {
        View footerMenu = findViewById(R.id.footer_menu);
        View footerReservation = findViewById(R.id.footer_reservation);

        if (footerMenu != null) footerMenu.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
        if (footerReservation != null) footerReservation.setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
    }
}