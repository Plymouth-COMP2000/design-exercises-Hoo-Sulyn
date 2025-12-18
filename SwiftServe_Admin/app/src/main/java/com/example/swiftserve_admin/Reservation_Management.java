package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.appcompat.widget.AppCompatSpinner;
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

    // The list for the Spinner
    String[] tableNames = {
            "Table 1 (2 pax)", "Table 2 (2 pax)", "Table 3 (4 pax)",
            "Table 4 (4 pax)", "Table 5 (4 pax)", "Table 6 (6 pax)",
            "Table 7 (6 pax)", "Table 8 (8 pax)", "Table 9 (8 pax)", "Table 10 (10 pax)"
    };

    // The matching capacities
    int[] tableCapacities = {2, 2, 4, 4, 4, 6, 6, 8, 8, 10};

    // Variable to track the total capacity being built for the current popup
    private int currentTotalCapacity = 0;

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
        allReservations = dbHelper.getAllReservations();
        List<Reservation> filtered = new ArrayList<>();

        String q = query.toLowerCase();
        for (Reservation r : allReservations) {
            if (query.isEmpty() ||
                    r.getGuestName().toLowerCase().contains(q) ||
                    r.getDate().toLowerCase().contains(q) ||
                    r.getStatus().toLowerCase().contains(q)) {
                filtered.add(r);
            }
        }

        // Apply the same sort to filtered results
        java.util.Collections.sort(filtered, (r1, r2) -> {
            return Integer.compare(getStatusWeight(r1.getStatus()), getStatusWeight(r2.getStatus()));
        });

        updateReservationCards(filtered);
    }

    private void updateReservationCards(List<Reservation> list) {
        cardsContainer.removeAllViews();

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

            // --- Standard Data Binding ---
            ((TextView) card.findViewById(R.id.reservation_id)).setText("RSV-" + r.getId());
            ((TextView) card.findViewById(R.id.card_guest_name)).setText(r.getGuestName());
            ((TextView) card.findViewById(R.id.card_guest_email)).setText(r.getEmail());
            ((TextView) card.findViewById(R.id.card_date)).setText(r.getDate());
            ((TextView) card.findViewById(R.id.card_time)).setText(r.getTime());
            ((TextView) card.findViewById(R.id.card_guests)).setText(String.valueOf(r.getGuests()));

            TextView detailsTv = card.findViewById(R.id.add_details);
            detailsTv.setText(r.getDetails());

            TextView tableNoTv = card.findViewById(R.id.table_number);
            tableNoTv.setText(r.getTableNum() != null ? r.getTableNum() : "-");

            TextView statusTv = card.findViewById(R.id.card_status);
            statusTv.setText(r.getStatus());

            // --- Find Dynamic View Elements ---
            LinearLayout pendingButtons = card.findViewById(R.id.pending_buttons_layout);
            MaterialButton vacantBtn = card.findViewById(R.id.card_vacant_btn);
            MaterialButton declinedLabel = card.findViewById(R.id.card_declined_status_label);
            LinearLayout reasonLayout = card.findViewById(R.id.reason_layout);
            TextView reasonTv = card.findViewById(R.id.card_reason);

            // --- Status Logic ---
            if ("Pending".equalsIgnoreCase(r.getStatus())) {
                statusTv.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

                pendingButtons.setVisibility(View.VISIBLE);
                vacantBtn.setVisibility(View.GONE);
                declinedLabel.setVisibility(View.GONE);
                reasonLayout.setVisibility(View.GONE);

                card.findViewById(R.id.card_accept_btn).setOnClickListener(v -> showAcceptPopup(r));
                card.findViewById(R.id.card_decline_btn).setOnClickListener(v -> showDeclinePopup(r));

            } else if ("Accepted".equalsIgnoreCase(r.getStatus())) {
                statusTv.setTextColor(Color.parseColor("#2EC23D")); // Bold Green text
                pendingButtons.setVisibility(View.GONE);
                vacantBtn.setVisibility(View.VISIBLE);
                vacantBtn.setEnabled(true);
                vacantBtn.setText("Set Vacant"); // Reset color to your default green

                vacantBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2EC23D")));
                vacantBtn.setTextColor(Color.WHITE);

                vacantBtn.setOnClickListener(v -> showVacancyPopup(r));

            } else if ("Attended".equalsIgnoreCase(r.getStatus())) {
                statusTv.setTextColor(Color.GRAY);
                statusTv.setText("Attended");

                pendingButtons.setVisibility(View.GONE);
                vacantBtn.setVisibility(View.VISIBLE);

                // BUTTON: "Reservation Attended" locked in Light Grey
                vacantBtn.setText("Reservation Attended");
                vacantBtn.setEnabled(false);
                vacantBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
                vacantBtn.setTextColor(Color.DKGRAY);

                declinedLabel.setVisibility(View.GONE);
                reasonLayout.setVisibility(View.GONE);

            } else if ("Declined". equalsIgnoreCase(r.getStatus())){
                statusTv.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                pendingButtons.setVisibility(View.GONE);
                vacantBtn.setVisibility(View.GONE);

                // Show the "Declined" label/button as unclickable
                declinedLabel.setVisibility(View.VISIBLE);
                declinedLabel.setText("Reservation Declined");
                declinedLabel.setEnabled(false);
                declinedLabel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));

                reasonLayout.setVisibility(View.VISIBLE);
                reasonTv.setText(r.getReason() != null ? r.getReason() : "No reason provided");
            }

            cardsContainer.addView(card);
        }
    }

    private void refreshReservationCards() {
        List<Reservation> reservations = dbHelper.getAllReservations();

        // Sort the list based on status priority
        java.util.Collections.sort(reservations, (r1, r2) -> {
            return Integer.compare(getStatusWeight(r1.getStatus()), getStatusWeight(r2.getStatus()));
        });

        updateReservationCards(reservations);
    }

    // Helper method to define the priority order
    private int getStatusWeight(String status) {
        if (status == null) return 4;
        switch (status.toLowerCase()) {
            case "pending":  return 0;
            case "accepted": return 1;
            case "declined": return 2;
            case "attended": return 3;
            default:         return 4;
        }
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
                    case "All": showByStatus(null); break;
                    case "Pending": showByStatus("Pending"); break;
                    case "Accepted": showByStatus("Accepted"); break;
                    case "Declined": showByStatus("Declined"); break;
                    case "Attended": showByStatus("Attended"); break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showByStatus(String status) {
        List<Reservation> all = dbHelper.getAllReservations();
        if (status == null) { // "All" selected
            java.util.Collections.sort(all, (r1, r2) ->
                    Integer.compare(getStatusWeight(r1.getStatus()), getStatusWeight(r2.getStatus())));
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

    private void showAcceptPopup(Reservation res) {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_accept_reservation_popup, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        LinearLayout spinnerContainer = dialogView.findViewById(R.id.spinner_container);
        TextView tvTotalGuests = dialogView.findViewById(R.id.total_guests);
        TextView tvEnough = dialogView.findViewById(R.id.enough_guest);
        AppCompatSpinner firstSpinner = dialogView.findViewById(R.id.table_dropdown);
        MaterialButton btnAddTable = dialogView.findViewById(R.id.btn_add_table);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        int guestsNeeded = res.getGuests();
        List<AppCompatSpinner> activeSpinners = new ArrayList<>();

        // --- 1. FILTER BUSY TABLES ---
        List<String> busyTables = dbHelper.getOccupiedTables();
        List<TableItem> availableItems = new ArrayList<>();

        for (int i = 0; i < tableNames.length; i++) {
            // Only add to the list if the table is NOT currently 'Accepted'
            if (!busyTables.contains(tableNames[i])) {
                availableItems.add(new TableItem(tableNames[i], tableCapacities[i], guestsNeeded));
            }
        }

        if (availableItems.isEmpty()) {
            Toast.makeText(this, "Fully Booked! No tables available.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- 2. SORT AND PREPARE DATA ---
        java.util.Collections.sort(availableItems, (a, b) -> Integer.compare(a.score, b.score));

        List<String> displayNames = new ArrayList<>();
        List<Integer> displayCaps = new ArrayList<>();
        for (TableItem item : availableItems) {
            displayNames.add(item.name);
            displayCaps.add(item.capacity);
        }

        // --- 3. SETUP FIRST SPINNER ---
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstSpinner.setAdapter(adapter);
        activeSpinners.add(firstSpinner);

        updateCapacityCheck(activeSpinners, displayCaps, guestsNeeded, tvTotalGuests, tvEnough);

        firstSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                updateCapacityCheck(activeSpinners, displayCaps, guestsNeeded, tvTotalGuests, tvEnough);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // --- 4. ADD TABLE BUTTON ---
        btnAddTable.setOnClickListener(v -> {
            com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 20);
            card.setLayoutParams(params);
            card.setRadius(8);
            card.setCardBackgroundColor(Color.WHITE);

            AppCompatSpinner newSpinner = new AppCompatSpinner(this);
            newSpinner.setAdapter(adapter);
            newSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                    updateCapacityCheck(activeSpinners, displayCaps, guestsNeeded, tvTotalGuests, tvEnough);
                }
                @Override public void onNothingSelected(AdapterView<?> p) {}
            });

            card.addView(newSpinner);
            spinnerContainer.addView(card);
            activeSpinners.add(newSpinner);
            updateCapacityCheck(activeSpinners, displayCaps, guestsNeeded, tvTotalGuests, tvEnough);
        });

        // --- 5. CONFIRM BUTTON ---
        btnConfirm.setOnClickListener(v -> {
            int total = 0;
            StringBuilder finalTables = new StringBuilder();
            for (AppCompatSpinner s : activeSpinners) {
                int pos = s.getSelectedItemPosition();
                total += displayCaps.get(pos);
                if (finalTables.length() > 0) finalTables.append(", ");
                finalTables.append(displayNames.get(pos));
            }

            if (total >= guestsNeeded) {
                dbHelper.updateStatusWithTable(res.getId(), "Accepted", finalTables.toString());
                dialog.dismiss();
                refreshReservationCards();
            } else {
                Toast.makeText(this, "Still not enough seats!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void showVacancyPopup(Reservation res) {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_table_vacancy_popup, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        // 1. Make the background transparent so your rounded corners show correctly
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView messageTv = dialogView.findViewById(R.id.vacancy_confirm_text);
        if (messageTv != null) {
            messageTv.setText("Are you sure you want to set " + res.getTableNum() + " as Vacant?");
        }

        MaterialButton confirmBtn = dialogView.findViewById(R.id.confirm_button);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.cancel_button);

        confirmBtn.setOnClickListener(v -> {
            dbHelper.updateStatus(res.getId(), "Attended");
            refreshReservationCards();
            dialog.dismiss();
            Toast.makeText(this, "Table is now vacant", Toast.LENGTH_SHORT).show();
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // SHOW the dialog first
        dialog.show();

        // force the width to match the screen
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // Helper method to update UI
    private void updateCapacityCheck(List<AppCompatSpinner> spinners, List<Integer> caps, int needed, TextView tvTotal, TextView tvEnough) {
        int current = 0;
        for (AppCompatSpinner s : spinners) {
            current += caps.get(s.getSelectedItemPosition());
        }

        tvTotal.setText(current + " / " + needed);
        if (current >= needed) {
            tvEnough.setText("(enough)");
            tvEnough.setTextColor(Color.parseColor("#2EC23D"));
        } else {
            tvEnough.setText("(not enough)");
            tvEnough.setTextColor(Color.RED);
        }
    }


    class TableItem {
        String name;
        int capacity;
        int score;

        TableItem(String name, int capacity, int needed) {
            this.name = name;
            this.capacity = capacity;

            if (capacity >= needed) {
                this.score = capacity - needed;
            } else {
                this.score = (needed - capacity) + 100;
            }
        }
    }

    private void showDeclinePopup(Reservation res) {
        // 1. INFLATE the decline layout
        View dialogView = getLayoutInflater().inflate(R.layout.activity_decline_reservation_popup, null);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView); // Use the inflated view object
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 2. FIND VIEWS inside 'dialogView'
        TextInputEditText etReasons = dialogView.findViewById(R.id.reasons);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // 3. SET LISTENERS
        btnConfirm.setOnClickListener(v -> {
            String reasonText = etReasons.getText() != null ? etReasons.getText().toString() : "";
            dbHelper.updateStatusWithReason(res.getId(), "Declined", reasonText);
            refreshReservationCards();
            dialog.dismiss();
            Toast.makeText(this, "Reservation declined.", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}