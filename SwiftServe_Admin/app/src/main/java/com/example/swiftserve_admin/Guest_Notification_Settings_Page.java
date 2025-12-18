package com.example.swiftserve_admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;

public class Guest_Notification_Settings_Page extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;
    private FrameLayout profileHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guest_notification_settings_page);

        // =======================
        // HEADER
        // =======================
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);         // top menu icon
        closeButton = findViewById(R.id.closeSideNav);     // sidebar close icon
        profileHeader = findViewById(R.id.profile_nav);    // header profile icon

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        profileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile_Settings.class)));

        // -----------------------
        // SIDEBAR BUTTONS
        // -----------------------
        View menuNav = findViewById(R.id.menuButton);
        View reservationNav = findViewById(R.id.reservationButton);
        View reservationHistoryNav = findViewById(R.id.reservationHistoryButton);
        View profileNav = findViewById(R.id.profileButton);
        View notificationSettingsNav = findViewById(R.id.notificationSettingsButton);
        MaterialButton logoutNav = findViewById(R.id.logoutButton);

        menuNav.setOnClickListener(v -> startActivity(new Intent(Guest_Notification_Settings_Page.this, Guest_Menu_Page.class)));
        reservationNav.setOnClickListener(v -> startActivity(new Intent(Guest_Notification_Settings_Page.this, Guest_Reservation_Page.class)));
        reservationHistoryNav.setOnClickListener(v -> startActivity(new Intent(Guest_Notification_Settings_Page.this, Reservation_History.class)));
        profileNav.setOnClickListener(v -> startActivity(new Intent(Guest_Notification_Settings_Page.this, Profile_Settings.class)));
        notificationSettingsNav.setOnClickListener(v -> startActivity(new Intent(Guest_Notification_Settings_Page.this, Guest_Notification_Settings_Page.class)));

        logoutNav.setOnClickListener(v -> {
            // Create a Dialog
            Dialog dialog = new Dialog(Guest_Notification_Settings_Page.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.activity_logout_confirmation_popup);
            dialog.setCancelable(true); // can cancel by tapping outside

            // Make the dialog fill the screen
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
            }

            // Find buttons inside dialog
            MaterialButton confirmBtn = dialog.findViewById(R.id.confirmCancel);
            MaterialButton cancelBtn = dialog.findViewById(R.id.noCancel);

            // Confirm logout
            confirmBtn.setOnClickListener(view -> {
                startActivity(new Intent(Guest_Notification_Settings_Page.this, MainActivity.class));
                finishAffinity(); // closes all previous activities
            });

            // Cancel logout
            cancelBtn.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

        // -----------------------
        // BACK BUTTON
        // -----------------------
        TextView back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(v -> startActivity(new Intent(Guest_Notification_Settings_Page.this, Guest_Profile_Settings.class)));
    }
}