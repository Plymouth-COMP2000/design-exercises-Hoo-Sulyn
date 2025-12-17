package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class Reservation_Management extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;
    private LinearLayout cardsContainer;
    private ReservationDatabaseHelper dbHelper;
    private TextInputEditText searchEdit;
    private List<Reservation> allReservations = new ArrayList<>();
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reservation_management);

        searchEdit = findViewById(R.id.search_bar);
        setupSearchBar();

        filterSpinner = findViewById(R.id.filter_spinner);
        setupFilterSpinner();

        dbHelper = new ReservationDatabaseHelper(this);
        cardsContainer = findViewById(R.id.reservation_cards_container);
        refreshReservationCards();   // fill the screen

        // =======================
        // HEADER
        // =======================
        drawerLayout = findViewById(R.id.main);
        menuButton = findViewById(R.id.side_nav_button);         // top menu icon
        closeButton = findViewById(R.id.closeSideNav);     // sidebar close icon
        profileHeader = findViewById(R.id.profile_nav);    // header profile icon

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));

        // -----------------------
        // SIDEBAR BUTTONS
        // -----------------------
        View dashboardNav = findViewById(R.id.dashboardButton);
        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        dashboardNav.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Notification_Settings.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());

        // -----------------------
        // FOOTER
        // -----------------------
        FrameLayout footer_dashboard = findViewById(R.id.footer_dashboard);
        FrameLayout footer_menu = findViewById(R.id.footer_menu);
        FrameLayout footer_reservation = findViewById(R.id.footer_reservation);

        footer_dashboard.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Dashboard.class)));
        footer_menu.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Menu_Management.class)));
        footer_reservation.setOnClickListener(v -> startActivity(new Intent(Reservation_Management.this, Reservation_Management.class)));

        // -----------------------
        // NOTIFICATION
        // -----------------------
        SharedPreferences prefs =
                getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        boolean enabled = prefs.getBoolean("enable_notifications", true);
        boolean incoming = prefs.getBoolean("incoming_reservations", false);

        if (enabled && incoming) {
            NotificationHelper.show(
                    this,
                    "New Reservation",
                    "A new reservation has been made"
            );
        }

        if (enabled && prefs.getBoolean("reservation_edits", false)) {
            NotificationHelper.show(
                    this,
                    "Reservation Updated",
                    "A reservation has been modified"
            );
        }

    }

    private void changeStatus(int id, String newStatus) {
        dbHelper.updateStatus(id, newStatus);
        Toast.makeText(this, "Reservation " + newStatus.toLowerCase(), Toast.LENGTH_SHORT).show();
        refreshReservationCards();   // redraw
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshReservationCards();
    }

    private void setupSearchBar() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int start,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                filterReservations(s.toString().trim());
            }
        });
    }

    private void filterReservations(String query) {
        allReservations = dbHelper.getAllReservations();   // fresh from DB
        List<Reservation> filtered = new ArrayList<>();

        if (query.isEmpty()) {
            filtered = allReservations;
        } else {
            String q = query.toLowerCase();
            for (Reservation r : allReservations) {
                if (r.getGuestName().toLowerCase().contains(q) ||
                        r.getDate().toLowerCase().contains(q)          ||
                        r.getStatus().toLowerCase().contains(q))
                    filtered.add(r);
            }
        }
        updateReservationCards(filtered);   // use overloaded version
    }

    private void updateReservationCards(List<Reservation> list) {
        cardsContainer.removeAllViews();          // start fresh

        if (list.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No reservations yet");
            tv.setTextSize(18);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 200, 0, 0);
            cardsContainer.addView(tv);
            return;
        }

        for (final Reservation r : list) {
            View card = getLayoutInflater().inflate(R.layout.reservation_card_template, cardsContainer, false);

            // --- populate ---
            ((TextView) card.findViewById(R.id.card_guest_name)).setText(r.getGuestName());
            ((TextView) card.findViewById(R.id.card_date)).setText(r.getDate());
            ((TextView) card.findViewById(R.id.card_time)).setText(r.getTime());
            ((TextView) card.findViewById(R.id.card_guests)).setText(String.valueOf(r.getGuests()));

            TextView statusTv = card.findViewById(R.id.card_status);
            statusTv.setText(r.getStatus());

            // colour by status
            int colour;
            switch (r.getStatus()) {
                case "Accepted": colour = android.R.color.holo_green_dark; break;
                case "Declined": colour = android.R.color.holo_red_dark;   break;
                default:         colour = android.R.color.holo_orange_dark;
            }
            statusTv.setTextColor(getResources().getColor(colour));

            // --- buttons ---
            MaterialButton acceptBtn = card.findViewById(R.id.card_accept_btn);
            MaterialButton declineBtn = card.findViewById(R.id.card_decline_btn);

            acceptBtn.setOnClickListener(v -> changeStatus(r.getId(), "Accepted"));
            declineBtn.setOnClickListener(v -> changeStatus(r.getId(), "Declined"));

            cardsContainer.addView(card);
        }
    }

    private void refreshReservationCards() {
        updateReservationCards(dbHelper.getAllReservations());
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.reservation_filters,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String choice = parent.getItemAtPosition(pos).toString();
                switch (choice) {
                    case "All":       showByStatus(null);           break;
                    case "Pending":   showByStatus("Pending");      break;
                    case "Accepted":  showByStatus("Accepted");     break;
                    case "Declined":  showByStatus("Declined");     break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showByStatus(String status) {
        List<Reservation> all = dbHelper.getAllReservations();
        if (status == null) {                 // "All"
            updateReservationCards(all);
            return;
        }
        List<Reservation> filtered = new ArrayList<>();
        for (Reservation r : all)
            if (r.getStatus().equalsIgnoreCase(status))
                filtered.add(r);
        updateReservationCards(filtered);
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Reservation_Management.this);
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

            Intent intent = new Intent(Reservation_Management.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
}