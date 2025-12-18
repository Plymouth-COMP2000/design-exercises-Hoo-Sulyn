package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;

public class Guest_Menu_Page extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;
    private RecyclerView menuRecycler;
    private MenuDatabaseHelper dbHelper;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems = new ArrayList<>();
    private TextInputEditText searchBar;
    private List<MenuItem> fullListForSearching = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_menu_page);

        dbHelper = new MenuDatabaseHelper(this);

        menuRecycler = findViewById(R.id.menu_recycler);
        menuRecycler.setLayoutManager(new GridLayoutManager(this, 2));


        adapter = new MenuAdapter(menuItems, item -> showItemDetails(item));
        menuRecycler.setAdapter(adapter);

        searchBar = findViewById(R.id.search_bar);

        setupSearch();

        loadMenuItems();

        setupUI();
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performFilter(s.toString());
            }
        });
    }

    private void performFilter(String query) {
        // 1. If the user clears the search bar
        if (query.isEmpty()) {
            loadMenuItems();
            return;
        }

        List<MenuItem> filteredList = new ArrayList<>();
        List<MenuItem> availableResults = new ArrayList<>();
        List<MenuItem> soldOutResults = new ArrayList<>();

        String lowerQuery = query.toLowerCase().trim();

        for (MenuItem item : fullListForSearching) {
            boolean matches = item.getName().toLowerCase().contains(lowerQuery) ||
                    item.getDescription().toLowerCase().contains(lowerQuery);

            if (matches) {
                if (item.getStatus().equalsIgnoreCase("Available")) {
                    availableResults.add(item);
                } else if (item.getStatus().equalsIgnoreCase("Sold Out")) {
                    soldOutResults.add(item);
                }
            }
        }

        // Combine results
        filteredList.addAll(availableResults);
        filteredList.addAll(soldOutResults);

        adapter.updateList(filteredList);
    }

    private void loadMenuItems() {
        fullListForSearching = dbHelper.getAllMenuItems();

        List<MenuItem> available = new ArrayList<>();
        List<MenuItem> soldOut = new ArrayList<>();

        for (MenuItem item : fullListForSearching) {
            if (item.getStatus().equalsIgnoreCase("Available")) {
                available.add(item);
            } else if (item.getStatus().equalsIgnoreCase("Sold Out")) {
                soldOut.add(item);
            }
        }

        // Clear the current list and swap in the sorted items
        menuItems.clear();
        menuItems.addAll(available);
        menuItems.addAll(soldOut);

        adapter.updateList(menuItems);
    }

    private void showItemDetails(MenuItem item) {
        // 1. Create the dialog with your custom theme
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        // 2. Set the content view to your XML layout
        bottomSheetDialog.setContentView(R.layout.menu_item_description);

        // 3. Find the views inside the dialog (note: you must use bottomSheetDialog.findViewById)
        ImageView image = bottomSheetDialog.findViewById(R.id.popup_item_image);
        TextView name = bottomSheetDialog.findViewById(R.id.popup_item_name);
        TextView price = bottomSheetDialog.findViewById(R.id.popup_item_price);
        TextView description = bottomSheetDialog.findViewById(R.id.popup_item_description);
        TextView allergen = bottomSheetDialog.findViewById(R.id.popup_allergen_info);

        // 4. Fill the data from the MenuItem object
        if (name != null) name.setText(item.getName());
        if (price != null) price.setText("RM " + String.format("%.2f", item.getPrice()));
        if (description != null) description.setText(item.getDescription());

        // Status or extra info can go into the allergen field for now
        if (allergen != null) allergen.setText("Status: " + item.getStatus());

        // 5. Show the popup
        bottomSheetDialog.show();
    }



    private void setupUI() {
        // --- HEADER ---
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);
        closeButton = findViewById(R.id.closeSideNav);
        profileHeader = findViewById(R.id.profile_nav);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Guest_Profile_Settings.class)));

        // --- SIDEBAR ---
        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View reservationHistoryNav = findViewById(R.id.reservationHistoryButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        menuNav.setOnClickListener(v -> startActivity(new Intent(Guest_Menu_Page.this, Guest_Menu_Page.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Guest_Menu_Page.this, Guest_Reservation_Page.class)));
        reservationHistoryNav.setOnClickListener(v -> startActivity(new Intent(Guest_Menu_Page.this, Reservation_History.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Guest_Menu_Page.this, Guest_Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Guest_Menu_Page.this, Guest_Notification_Settings_Page.class)));

        logoutNav.setOnClickListener(v -> showLogoutDialog());

        // --- FOOTER ---
        findViewById(R.id.footer_reservation).setOnClickListener(v -> startActivity(new Intent(this, Guest_Reservation_Page.class)));
        findViewById(R.id.footer_history).setOnClickListener(v -> startActivity(new Intent(this, Reservation_History.class)));
    }



    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_logout_confirmation_popup);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.findViewById(R.id.confirmCancel).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
        dialog.findViewById(R.id.noCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}