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

public class Guest_Edit_Reservation extends GuestPollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private FrameLayout profileHeader;
    private TextInputEditText etDate, etTime, etGuests, etFirstName, etLastName, etEmail, etPhone, etDetails;
    private MaterialButton btnSave;
    private long reservationId = -1;
    private Calendar calendar = Calendar.getInstance();

    // Greeting logic
    private UserService userService;
    private String studentId = "bsse2509244";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_edit_reservation);

        userService = new UserService(this);

        // 1. INITIALIZE VIEWS & PICKERS
        initViews();
        setupPickers();

        // 2. HEADER & SIDEBAR LOGIC
        setupNavigation();

        // 3. LOAD DATA
        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1);
        if (reservationId != -1) {
            loadReservationData();
        }

        btnSave.setOnClickListener(v -> updateReservation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserGreeting(); // Refresh "Hello, Name!"
    }

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));

        // --- SIDEBAR (edit_reservation_nav) ---
        View sideNavView = findViewById(R.id.edit_reservation_nav);
        if (sideNavView != null) {
            sideNavView.findViewById(R.id.menuButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
            sideNavView.findViewById(R.id.reservationButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
            sideNavView.findViewById(R.id.reservationHistoryButton).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
            sideNavView.findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));
            sideNavView.findViewById(R.id.closeSideNav).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

            MaterialButton logoutNav = sideNavView.findViewById(R.id.logoutButton);
            logoutNav.setOnClickListener(v -> showLogoutDialog());
        }

        // --- FOOTER ---
        findViewById(R.id.footer_menu).setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));
        findViewById(R.id.footer_reservation).setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
        findViewById(R.id.footer_history).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
    }

    private void loadUserGreeting() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("logged_in_user_id", "");
        if (userId.isEmpty()) return;

        userService.getUserProfile(studentId, userId, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                View sideNavView = findViewById(R.id.edit_reservation_nav);
                if (sideNavView != null) {
                    TextView greeting = sideNavView.findViewById(R.id.guest_greeting_name);
                    if (greeting != null) greeting.setText("Hello, " + user.getFirstname() + "!");
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void initViews() {
        etDate = findViewById(R.id.date);
        etTime = findViewById(R.id.time);
        etGuests = findViewById(R.id.total_guests);
        etFirstName = findViewById(R.id.firstname);
        etLastName = findViewById(R.id.lastname);
        etEmail = findViewById(R.id.email);
        etPhone = findViewById(R.id.phone_num);
        etDetails = findViewById(R.id.add_details);
        btnSave = findViewById(R.id.save_changes_button);
    }

    private void loadReservationData() {
        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        Reservation res = db.getReservationById(reservationId);

        if (res != null) {
            etDate.setText(res.getDate());
            etTime.setText(res.getTime());
            etGuests.setText(String.valueOf(res.getGuests()));
            etEmail.setText(res.getEmail());
            etPhone.setText(res.getPhone());
            etDetails.setText(res.getDetails());

            String fullName = res.getGuestName();
            if (fullName != null && fullName.contains(" ")) {
                String[] parts = fullName.split(" ", 2);
                etFirstName.setText(parts[0]);
                etLastName.setText(parts[1]);
            } else {
                etFirstName.setText(fullName);
            }
        }
    }

    private void updateReservation() {
        String fullName = etFirstName.getText().toString().trim() + " " + etLastName.getText().toString().trim();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String guestStr = etGuests.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();
        String details = etDetails.getText().toString();

        if (date.isEmpty() || time.isEmpty() || guestStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int guests = Integer.parseInt(guestStr);
        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);
        db.fullUpdateReservation(reservationId, fullName, email, phone, date, time, guests, details);

        Toast.makeText(this, "Reservation Updated Successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Reservation_History.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setupPickers() {
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String format = "dd/MM/yyyy";
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, Locale.US);
                etDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                int hour = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
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
        dialog.findViewById(R.id.confirmCancel).setOnClickListener(view -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
}