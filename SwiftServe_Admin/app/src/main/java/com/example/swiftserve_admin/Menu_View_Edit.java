package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

public class Menu_View_Edit extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;
    private com.google.android.material.textfield.TextInputEditText editName, editPrice, editDesc;
    private com.google.android.material.textfield.TextInputEditText editAllergenContains, editAllergenMayContain;
    private android.widget.Spinner spinnerStatus;
    private MaterialButton saveChangesButton;
    private int currentMenuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_view_edit);

        // -----------------------
        // Get the menu ID from intent
        // -----------------------
        Intent intent = getIntent();
        currentMenuId = intent.getIntExtra("MENU_ID", -1);

        if (currentMenuId == -1) {
            Toast.makeText(this, "Error: No mex`nu item selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI
        initViews();

        // Load existing data
        loadMenuItemData(currentMenuId);

        // Setup save button
        setupSaveButton();

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

        dashboardNav.setOnClickListener(v -> startActivity(new Intent(Menu_View_Edit.this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(Menu_View_Edit.this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Menu_View_Edit.this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Menu_View_Edit.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Menu_View_Edit.this, Notification_Settings.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());

        // -----------------------
        // BACK BUTTON
        // -----------------------
        TextView back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(v -> finish());
    }

    private void initViews() {
        // Input fields
        editName = findViewById(R.id.new_item_name);
        editPrice = findViewById(R.id.new_item_price);
        editDesc = findViewById(R.id.new_item_desc);
        spinnerStatus = findViewById(R.id.status_dropdown);
        editAllergenContains = findViewById(R.id.new_item_allergen_contains);
        editAllergenMayContain = findViewById(R.id.new_item_allergen_may_contain);
        saveChangesButton = findViewById(R.id.save_changes);

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void loadMenuItemData(int menuId) {
        MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(this);
        MenuItem menuItem = dbHelper.getMenuItem(menuId);

        if (menuItem != null) {
            editName.setText(menuItem.getName());
            editPrice.setText(String.valueOf(menuItem.getPrice()));

            // Parse description to separate parts
            String fullText = menuItem.getDescription();
            String description = "";
            String contains = "";
            String mayContain = "";

            if (fullText.contains("\n\nContains:")) {
                String[] parts = fullText.split("\n\nContains:");
                description = parts[0].trim();

                if (parts.length > 1) {
                    String allergenPart = parts[1];
                    if (allergenPart.contains("\nMay contain:")) {
                        String[] allergenParts = allergenPart.split("\nMay contain:");
                        contains = allergenParts[0].trim();
                        if (allergenParts.length > 1) {
                            mayContain = allergenParts[1].trim();
                        }
                    } else {
                        contains = allergenPart.trim();
                    }
                }
            } else {
                description = fullText; // No allergen info
            }

            // Set the separated values
            editDesc.setText(description);
            editAllergenContains.setText(contains);
            editAllergenMayContain.setText(mayContain);

            // Set spinner selection
            for (int i = 0; i < spinnerStatus.getCount(); i++) {
                if (spinnerStatus.getItemAtPosition(i).toString().equalsIgnoreCase(menuItem.getStatus())) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        } else {
            Toast.makeText(this, "Menu item not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSaveButton() {
        saveChangesButton.setOnClickListener(v -> {
            // Get updated values
            String name = editName.getText().toString().trim();
            String priceStr = editPrice.getText().toString().trim();
            String description = editDesc.getText().toString().trim(); // Just the description
            String status = spinnerStatus.getSelectedItem().toString();
            String allergenContains = editAllergenContains.getText().toString().trim();
            String allergenMayContain = editAllergenMayContain.getText().toString().trim();

            // Validate
            if (name.isEmpty()) {
                editName.setError("Name is required");
                editName.requestFocus();
                return;
            }

            if (priceStr.isEmpty()) {
                editPrice.setError("Price is required");
                editPrice.requestFocus();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);

                // Combine description with allergen info
                String fullDescription = description;
                if (!allergenContains.isEmpty()) {
                    fullDescription += "\n\nContains: " + allergenContains;
                }
                if (!allergenMayContain.isEmpty()) {
                    fullDescription += "\nMay contain: " + allergenMayContain;
                }

                // Update in database
                MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(this);
                int rowsAffected = dbHelper.updateMenuItem(currentMenuId, name, price, fullDescription, status);

                if (rowsAffected > 0) {
                    Toast.makeText(this, "Menu item updated successfully", Toast.LENGTH_SHORT).show();

                    // Go back to Menu Management
                    Intent intent = new Intent(Menu_View_Edit.this, Menu_Management.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update menu item", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                editPrice.setError("Invalid price format");
                editPrice.requestFocus();
            }
        });
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Menu_View_Edit.this);
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

            Intent intent = new Intent(Menu_View_Edit.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
}