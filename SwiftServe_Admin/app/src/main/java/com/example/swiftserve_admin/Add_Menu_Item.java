package com.example.swiftserve_admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

public class Add_Menu_Item extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;

    // Input fields
    private TextInputEditText editName, editPrice, editDesc;
    private TextInputEditText editAllergenContains, editAllergenMayContain;
    private Spinner spinnerStatus;
    private MaterialButton attachImageButton;
    private TextView attachFileName;
    private ImageView imagePreview;

    // Image picking
    private static final int PICK_IMAGE_REQUEST = 100;
    private String imagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_menu_item);

        // Initialize all UI elements
        initViews();

        // Setup spinner
        setupSpinner();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initViews() {
        // Header elements
        drawerLayout = findViewById(R.id.main);
        menuButton = findViewById(R.id.side_nav_button);
        closeButton = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);

        // Input fields
        editName = findViewById(R.id.new_item_name);
        editPrice = findViewById(R.id.new_item_price);
        editDesc = findViewById(R.id.new_item_desc);
        spinnerStatus = findViewById(R.id.status_dropdown);
        editAllergenContains = findViewById(R.id.new_item_allergen_contains);
        editAllergenMayContain = findViewById(R.id.new_item_allergen_may_contain);
        attachImageButton = findViewById(R.id.attach_image_button);
        attachFileName = findViewById(R.id.attach_file_name);
        imagePreview = findViewById(R.id.image_preview);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        // =======================
        // HEADER BUTTONS
        // =======================
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

        dashboardNav.setOnClickListener(v -> startActivity(new Intent(Add_Menu_Item.this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(Add_Menu_Item.this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Add_Menu_Item.this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Add_Menu_Item.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Add_Menu_Item.this, Notification_Settings.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());

        // -----------------------
        // BACK BUTTON
        // -----------------------
        TextView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> finish());

        // -----------------------
        // ATTACH IMAGE BUTTON - ACTUAL IMAGE PICKING
        // -----------------------
        attachImageButton.setOnClickListener(v -> pickImageFromGallery());

        // -----------------------
        // ADD ITEM BUTTON
        // -----------------------
        MaterialButton add_item_button = findViewById(R.id.add_item_button);
        add_item_button.setOnClickListener(v -> addMenuItemToDatabase());
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            // Get the image path
            imagePath = getImagePathFromUri(selectedImageUri);

            // Display image preview
            imagePreview.setImageURI(selectedImageUri);
            attachFileName.setText("Image selected: " + getFileNameFromPath(imagePath));
        }
    }

    private String getImagePathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    private String getFileNameFromPath(String path) {
        if (path == null || path.isEmpty()) return "Unknown";
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private void addMenuItemToDatabase() {
        // Get values from inputs
        String name = editName.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String description = editDesc.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String allergenContains = editAllergenContains.getText().toString().trim();
        String allergenMayContain = editAllergenMayContain.getText().toString().trim();

        // Validate required fields
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

            // Insert into SQLite database
            MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(Add_Menu_Item.this);

            // If you want to store image path, update your database and use:
            // long newId = dbHelper.insertMenuItem(name, price, fullDescription, status, imagePath);

            // For now, without image column:
            long newId = dbHelper.insertMenuItem(name, price, fullDescription, status);

            if (newId != -1) {
                Toast.makeText(this, "Menu item added successfully", Toast.LENGTH_SHORT).show();

                // Clear form
                clearForm();

                // Go back to Menu Management
                Intent intent = new Intent(Add_Menu_Item.this, Menu_Management.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to add menu item", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            editPrice.setError("Invalid price format");
            editPrice.requestFocus();
        }
    }

    private void clearForm() {
        editName.setText("");
        editPrice.setText("");
        editDesc.setText("");
        editAllergenContains.setText("");
        editAllergenMayContain.setText("");
        spinnerStatus.setSelection(0);
        attachFileName.setText("");
        imagePreview.setImageResource(android.R.color.transparent);
        imagePath = "";
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Add_Menu_Item.this);
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

            Intent intent = new Intent(Add_Menu_Item.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
}