package com.example.swiftserve_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Guest_Confirmation_Page extends GuestPollingBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_confirmation_page);

        // 1. Find the TextView where the ID should go
        TextView reservationIdTv = findViewById(R.id.reservation_id);
        MaterialButton return_to_menu = findViewById(R.id.return_to_menu_button);

        // 2. GET the ID passed from the previous activity
        // We use -1 as a default value just in case nothing was passed
        long resId = getIntent().getLongExtra("RESERVATION_ID", -1);

        // 3. DISPLAY the ID (formatted with RSV-)
        if (resId != -1) {
            reservationIdTv.setText("RSV-" + resId);
        } else {
            reservationIdTv.setText("N/A");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return_to_menu.setOnClickListener(v -> {
            startActivity(new Intent(Guest_Confirmation_Page.this, Guest_Menu_Page.class));
            finish(); // Finish this page so they don't accidentally go "back" to it
        });
    }
}