package com.example.swift_serve;

import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton loginButton, registerOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        registerOption = findViewById(R.id.registerOption);

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
    }
}