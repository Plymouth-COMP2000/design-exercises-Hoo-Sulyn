package com.example.swift_serve;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class Menu_Side_Nav extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_side_nav);

        MaterialCardView reservationButton = findViewById(R.id.reservationButton);
        MaterialCardView reservationHistoryButton = findViewById(R.id.reservationHistoryButton);
        MaterialCardView profileButton = findViewById(R.id.profileButton);
        MaterialCardView notificationSettingsButton = findViewById(R.id.notificationSettingsButton);
        MaterialCardView menuButton = findViewById(R.id.menuButton);

        MaterialButton logoutButton = findViewById(R.id.logoutButton);

        reservationButton.setOnClickListener(v -> {
            Intent i = new Intent(this, Reservation_Page.class);
            startActivity(i);
        });

        reservationHistoryButton.setOnClickListener(v -> {
            Intent i = new Intent(this, Reservation_History.class);
            startActivity(i);
        });

        profileButton.setOnClickListener(v -> {
            Intent i = new Intent(this, Profile_Settings.class);
            startActivity(i);
        });

        notificationSettingsButton.setOnClickListener(v -> {
            Intent i = new Intent(this, Notification_Settings_Page.class);
            startActivity(i);
        });

        menuButton.setOnClickListener(v -> {
            Intent i = new Intent(this, Menu_Page.class);
            startActivity(i);
        });

        logoutButton.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        });
    }
}
