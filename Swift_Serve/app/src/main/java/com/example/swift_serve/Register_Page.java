package com.example.swift_serve;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Register_Page extends AppCompatActivity {

    private MaterialButton registerButton, loginOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        registerButton = findViewById(R.id.registerButton);
        loginOption = findViewById(R.id.loginOption);

        // REGISTER BUTTON -> open Menu_Page
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Register_Page.this, Menu_Page.class);
            startActivity(intent);
        });

        // LOGIN BUTTON -> open Login_Page
        loginOption.setOnClickListener(v -> {
            Intent intent = new Intent(Register_Page.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}