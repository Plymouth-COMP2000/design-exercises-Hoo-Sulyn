package com.example.mal2017_project_swiftserve;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class Menu_Page extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton, closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_page);

        // Find views
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.imageView);       // Menu icon in header
        closeButton = findViewById(R.id.closeSideNav);   // Close button inside drawer

        // Open drawer when menu icon is clicked
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Close drawer when close button is clicked
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        // Close drawer when tapping the dimmed overlay
        View overlay = findViewById(R.id.sideNavOverlay);
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }
}