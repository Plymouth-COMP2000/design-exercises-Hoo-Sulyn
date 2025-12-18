package com.example.swiftserve_admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class Guest_Reservation_Page extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;

    // Input Fields
    private TextInputEditText dateEdit, timeEdit, guestsEdit, firstNameEdit, lastNameEdit, emailEdit, phoneEdit, detailsEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_reservation_page);

        initHeaderAndSidebar();
        initFormFields();
        loadUserData(); // Auto-fill personal info
        initFooter();
    }

    private void initFormFields() {
        // Map XML IDs
        dateEdit = findViewById(R.id.date);
        timeEdit = findViewById(R.id.time);
        guestsEdit = findViewById(R.id.num_guests);
        firstNameEdit = findViewById(R.id.first_name);
        lastNameEdit = findViewById(R.id.last_name);
        emailEdit = findViewById(R.id.email);
        phoneEdit = findViewById(R.id.phone_num);
        detailsEdit = findViewById(R.id.add_details);

        // Date Picker
        dateEdit.setFocusable(false);
        dateEdit.setOnClickListener(v -> showDatePicker());

        // Time Picker
        timeEdit.setFocusable(false);
        timeEdit.setOnClickListener(v -> showTimePicker());

        // View Summary Button logic
        MaterialButton viewSummary = findViewById(R.id.view_summary_button);
        if (viewSummary != null) {
            viewSummary.setOnClickListener(v -> proceedToSummary());
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Get username saved during Login
        String username = prefs.getString("user_name", "");
        String studentId = "bsse2509244"; // Your specific student ID

        if (username.isEmpty()) return;

        RequestQueue queue = Volley.newRequestQueue(this);

        // Updated URL format to target a single user: .../student_id/username
        String url = "http://10.240.72.69/comp2000/coursework/read_user/" + studentId + "/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject user = response;
                        if (response.has("user")) {
                            user = response.getJSONObject("user");
                        }

                        String email = user.optString("email", "");
                        String phone = user.optString("contact", "");
                        String firstName = user.optString("firstname", username);
                        String lastName = user.optString("lastname", "");

                        runOnUiThread(() -> {
                            firstNameEdit.setText(firstName);
                            lastNameEdit.setText(lastName);
                            emailEdit.setText(email);
                            phoneEdit.setText(phone);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error fetching profile", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
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
        // 1. Get current values
        String fullName = firstNameEdit.getText().toString().trim() + " " + lastNameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();
        String date = dateEdit.getText().toString().trim();
        String time = timeEdit.getText().toString().trim();
        String guests = guestsEdit.getText().toString().trim();
        String details = detailsEdit.getText().toString().trim();

        // 2. Validation
        if (fullName.isEmpty() || date.isEmpty() || guests.isEmpty()) {
            Toast.makeText(this, "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Pass to Summary
        Intent intent = new Intent(this, Guest_Reservation_Summary.class);
        intent.putExtra("GUEST_NAME", fullName.trim());
        intent.putExtra("EMAIL", email);
        intent.putExtra("PHONE", phone);
        intent.putExtra("DATE", date);
        intent.putExtra("TIME", time);
        intent.putExtra("GUESTS", guests);
        intent.putExtra("DETAILS", details);

        startActivity(intent);
    }

    private void initHeaderAndSidebar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        closeButton = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));

        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View reservationHistoryNav = findViewById(R.id.reservationHistoryButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        menuNav.setOnClickListener(v -> startActivity(new Intent(Guest_Reservation_Page.this, Guest_Reservation_Page.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Guest_Reservation_Page.this, Guest_Reservation_Page.class)));
        reservationHistoryNav.setOnClickListener(v -> startActivity(new Intent(Guest_Reservation_Page.this, Reservation_History.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Guest_Reservation_Page.this, Guest_Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Guest_Reservation_Page.this, Guest_Notification_Settings_Page.class)));
    }

    private void initFooter() {
        View footerMenu = findViewById(R.id.footer_main);
        View footerHistory = findViewById(R.id.footer_history);

        if (footerMenu != null)
            footerMenu.setOnClickListener(v -> startActivity(new Intent(this, Guest_Menu_Page.class)));

        if (footerHistory != null)
            footerHistory.setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
    }
}