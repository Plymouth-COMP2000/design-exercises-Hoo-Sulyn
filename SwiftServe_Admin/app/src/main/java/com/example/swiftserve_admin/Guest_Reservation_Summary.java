package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

public class Guest_Reservation_Summary extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;

    // Summary TextViews
    private TextView tvName, tvEmail, tvPhone, tvDate, tvTime, tvGuests, tvComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_reservation_summary);

        initViews();
        displayReservationData();
        setupNavigation();

        MaterialButton confirmBtn = findViewById(R.id.confirm_reservation_button);
        confirmBtn.setOnClickListener(v -> confirmReservation());
    }

    private void initViews() {
        // Initialize TextViews using your XML IDs
        tvName = findViewById(R.id.full_name);
        tvEmail = findViewById(R.id.email);
        tvPhone = findViewById(R.id.phone_num);
        tvDate = findViewById(R.id.reserve_date);
        tvTime = findViewById(R.id.reserve_time);
        tvGuests = findViewById(R.id.total_guests);
        tvComment = findViewById(R.id.add_comment);

        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        closeButton = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);
    }

    private void displayReservationData() {
        // Retrieve data from Intent sent by Guest_Reservation_Page
        Intent intent = getIntent();
        String name = intent.getStringExtra("GUEST_NAME");
        String email = intent.getStringExtra("EMAIL");
        String phone = intent.getStringExtra("PHONE");
        String date = intent.getStringExtra("DATE");
        String time = intent.getStringExtra("TIME");
        String guests = intent.getStringExtra("GUESTS");
        String comment = intent.getStringExtra("DETAILS");

        // Set text to TextViews
        if (tvName != null) tvName.setText(name);
        if (tvEmail != null) tvEmail.setText(email);
        if (tvPhone != null) tvPhone.setText(phone);
        if (tvDate != null) tvDate.setText(date);
        if (tvTime != null) tvTime.setText(time);
        if (tvGuests != null) tvGuests.setText(guests);
        if (tvComment != null) {
            tvComment.setText((comment == null || comment.isEmpty()) ? "None" : comment);
        }
    }

    private void confirmReservation() {
        // 1. Get the data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("GUEST_NAME");
        String email = intent.getStringExtra("EMAIL");
        String phone = intent.getStringExtra("PHONE");
        String date = intent.getStringExtra("DATE");
        String time = intent.getStringExtra("TIME");
        String guestsStr = intent.getStringExtra("GUESTS");
        String details = intent.getStringExtra("DETAILS");

        // 2. Safely convert Guest String to Integer
        int guestCount = 1;
        try {
            if (guestsStr != null) guestCount = Integer.parseInt(guestsStr);
        } catch (NumberFormatException e) {
            guestCount = 1;
        }

        // 3. Save to Database
        ReservationDatabaseHelper db = new ReservationDatabaseHelper(this);

        // This matches the order in your Helper:
        // (name, email, phone, date, time, guests, status, details)
        boolean isSaved = db.addReservation(
                name,
                email,
                phone,
                date,
                time,
                guestCount,
                "Pending",
                details
        );

        if (isSaved) {
            Toast.makeText(this, "Reservation Confirmed!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Guest_Confirmation_Page.class));
            finishAffinity();
        } else {
            Toast.makeText(this, "Error saving reservation", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigation() {
        // Back Button logic
        TextView backBtn = findViewById(R.id.back_button);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        // Header and Sidebar Logic (Keep your existing menu/sidebar listeners here)
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        // ... (remaining sidebar buttons code)
    }
}