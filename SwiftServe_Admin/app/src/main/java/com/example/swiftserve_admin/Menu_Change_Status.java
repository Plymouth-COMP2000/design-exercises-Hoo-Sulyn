package com.example.swiftserve_admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Menu_Change_Status extends AppCompatActivity {
    private int menuId;
    private String currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_change_status);

        // Get menu ID and current status from intent
        Intent intent = getIntent();
        menuId = intent.getIntExtra("MENU_ID", -1);
        currentStatus = intent.getStringExtra("CURRENT_STATUS");

        if (menuId == -1) {
            Toast.makeText(this, "Error: No menu item selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        MaterialButton btnAvailable = findViewById(R.id.available_button);
        MaterialButton btnSoldOut = findViewById(R.id.sold_out_button);
        MaterialButton btnUnavailable = findViewById(R.id.unavailable_button);

        btnAvailable.setOnClickListener(v -> updateStatus("Available"));
        btnSoldOut.setOnClickListener(v -> updateStatus("Sold Out"));
        btnUnavailable.setOnClickListener(v -> updateStatus("Unavailable"));
    }

    private void updateStatus(String newStatus) {
        MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(this);

        // Make sure you have this method in MenuDatabaseHelper.java
        int rowsAffected = dbHelper.updateMenuItemStatus(menuId, newStatus);

        if (rowsAffected > 0) {
            Toast.makeText(this, "Status changed to: " + newStatus, Toast.LENGTH_SHORT).show();

            // Go back to Menu Management
            Intent intent = new Intent(this, Menu_Management.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
        }
    }
}