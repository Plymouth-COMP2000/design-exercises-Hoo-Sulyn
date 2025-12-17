package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class Menu_Management extends PollingBaseActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;
    private TextInputEditText searchEdit;
    private List<MenuItem> allMenuItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_management);

        initViews();
        setupSidebar();
        setupSearchBar();
        refreshMenuCards();
    }

    private void initViews() {
        // Header
        drawerLayout = findViewById(R.id.main);
        menuButton = findViewById(R.id.side_nav_button);
        closeButton = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));

        // Add menu item button
        MaterialCardView add_menu_item_button = findViewById(R.id.add_menu_item_button);
        add_menu_item_button.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Add_Menu_Item.class)));

        // Footer
        FrameLayout footer_dashboard = findViewById(R.id.footer_dashboard);
        FrameLayout footer_menu = findViewById(R.id.footer_menu);
        FrameLayout footer_reservation = findViewById(R.id.footer_reservation);

        footer_dashboard.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Dashboard.class)));
        footer_menu.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Menu_Management.class)));
        footer_reservation.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Reservation_Management.class)));

        // Search bar
        searchEdit = findViewById(R.id.search_bar);
    }

    private void setupSidebar() {
        View dashboardNav = findViewById(R.id.dashboardButton);
        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        dashboardNav.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Dashboard.class)));
        menuNav.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Menu_Management.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Reservation_Management.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Menu_Management.this, Notification_Settings.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupSearchBar() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int start,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                filterMenuItems(s.toString().trim());
            }
        });
    }

    private void refreshMenuCards() {
        MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(this);
        allMenuItems = dbHelper.getAllMenuItems();
        updateMenuCards(allMenuItems);
    }

    private void filterMenuItems(String query) {
        List<MenuItem> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList = allMenuItems;
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (MenuItem item : allMenuItems) {
                if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                        item.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                        item.getStatus().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(item);
                }
            }
        }

        updateMenuCards(filteredList);
    }

    private void updateMenuCards(List<MenuItem> items) {
        LinearLayout cardsContainer = findViewById(R.id.menu_cards);
        cardsContainer.removeAllViews();

        for (MenuItem item : items) {
            View cardView = getLayoutInflater().inflate(R.layout.menu_card_template, cardsContainer, false);

            // Find views
            TextView nameView = cardView.findViewById(R.id.card_name);
            TextView priceView = cardView.findViewById(R.id.card_price);
            TextView statusView = cardView.findViewById(R.id.card_status);
            TextView changeStatusText = cardView.findViewById(R.id.change_status);
            TextView viewMoreBtn = cardView.findViewById(R.id.view_more_edit);
            Spinner statusSpinner = cardView.findViewById(R.id.status_spinner);
            MaterialButton deleteBtn = cardView.findViewById(R.id.delete_button);

            // Set data
            nameView.setText(item.getName());
            priceView.setText("RM " + String.format("%.2f", item.getPrice()));
            statusView.setText(item.getStatus());

            // Set status color
            String status = item.getStatus();
            if (status.equals("Available")) {
                statusView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (status.equals("Sold Out")) {
                statusView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (status.equals("Unavailable")) {
                statusView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            // Setup spinner
            setupStatusSpinner(statusSpinner, item, statusView);

            // Change status button
            changeStatusText.setOnClickListener(v -> {
                statusSpinner.setVisibility(View.VISIBLE);
                statusSpinner.performClick();
            });

            // View/Edit button
            viewMoreBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Menu_Management.this, Menu_View_Edit.class);
                intent.putExtra("MENU_ID", item.getId());
                startActivity(intent);
            });

            // Delete button
            deleteBtn.setOnClickListener(v -> showDeleteDialog(item.getId()));

            // Add card to container
            cardsContainer.addView(cardView);
        }
    }

    private void setupStatusSpinner(Spinner spinner, MenuItem item, TextView statusView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Available", "Sold Out", "Unavailable"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set current selection
        if (item.getStatus().equalsIgnoreCase("Available")) {
            spinner.setSelection(0);
        } else if (item.getStatus().equalsIgnoreCase("Sold Out")) {
            spinner.setSelection(1);
        } else {
            spinner.setSelection(2);
        }

        // Handle selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private String originalStatus = item.getStatus();

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newStatus = parent.getItemAtPosition(position).toString();

                if (!newStatus.equals(originalStatus)) {
                    MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(Menu_Management.this);
                    dbHelper.updateMenuItemStatus(item.getId(), newStatus);

                    Toast.makeText(Menu_Management.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();

                    // Update UI
                    spinner.setVisibility(View.GONE);
                    statusView.setText(newStatus);

                    // Update color
                    if (newStatus.equals("Available")) {
                        statusView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else if (newStatus.equals("Sold Out")) {
                        statusView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else if (newStatus.equals("Unavailable")) {
                        statusView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }

                    // Refresh the list to update all cards
                    refreshMenuCards();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDeleteDialog(final int menuId) {
        Dialog dialog = new Dialog(Menu_Management.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_menu_delete_confirmation_popup);
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
            MenuDatabaseHelper dbHelper = new MenuDatabaseHelper(Menu_Management.this);
            dbHelper.deleteMenuItem(menuId);
            dialog.dismiss();
            refreshMenuCards();
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Menu_Management.this);
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

            Intent intent = new Intent(Menu_Management.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMenuCards();
    }
}