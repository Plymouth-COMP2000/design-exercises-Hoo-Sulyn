package com.example.mal2017_project_swiftserve;

import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton loginButton, registerOption, adminOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // your login XML

        loginButton = findViewById(R.id.loginButton);
        registerOption = findViewById(R.id.registerOption);
        adminOption = findViewById(R.id.adminOption);

        // LOGIN BUTTON -> open Menu_Page
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Menu_Page.class);
            startActivity(intent);
        });

        // REGISTER BUTTON -> open Register_Page
        registerOption.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register_Page.class);
            startActivity(intent);
        });

        // ADMIN BUTTON -> open Admin_Login
        adminOption.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Admin_Login.class);
            startActivity(intent);
        });
    }
}
