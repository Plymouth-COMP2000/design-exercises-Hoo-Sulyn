package com.example.swiftserve_admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private MaterialButton loginButton, registerOption;
    private TextInputEditText loginEmail, loginPassword;
    private TextView forgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        registerOption = findViewById(R.id.registerOption);
        forgetPassword = findViewById(R.id.forget_password);

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://10.240.72.69/comp2000/coursework/read_all_users/bsse2509244";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray users = response.getJSONArray("users");
                            boolean emailFound = false;
                            boolean passwordCorrect = false;
                            JSONObject matchedUser = null;

                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                if (email.equalsIgnoreCase(user.getString("email"))) {
                                    emailFound = true;
                                    if (password.equals(user.getString("password"))) {
                                        passwordCorrect = true;
                                        matchedUser = user;
                                    }
                                    break;
                                }
                            }

                            if (!emailFound) {
                                Toast.makeText(this, "Email not registered. Please register.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, Register_Page.class));
                                return;
                            }

                            if (!passwordCorrect) {
                                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // --- MODIFIED LOGIC START ---
                            String userType = matchedUser.getString("usertype");

                            // Save Session Data
                            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                            editor.putBoolean("is_logged_in", true);
                            editor.putString("user_type", userType);
                            editor.putString("user_name", matchedUser.getString("username"));
                            editor.putString("user_email", email);
                            editor.putString("logged_in_student_id", "bsse2509244");

                            // Using customer ID or username as ID
                            editor.putString("logged_in_user_id", matchedUser.optString("id", matchedUser.getString("username")));
                            editor.apply();

                            // Redirect based on userType
                            if (userType.equalsIgnoreCase("admin")) {
                                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                                // Redirect Admin to Dashboard
                                startActivity(new Intent(MainActivity.this, Dashboard.class));
                            } else {
                                Toast.makeText(this, "Guest Login Successful", Toast.LENGTH_SHORT).show();
                                // Redirect Guest to Menu Page
                                startActivity(new Intent(MainActivity.this, Guest_Menu_Page.class));
                            }
                            finish();
                            // --- MODIFIED LOGIC END ---

                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            );
            queue.add(request);
        });

        registerOption.setOnClickListener(v -> startActivity(new Intent(this, Register_Page.class)));
        forgetPassword.setOnClickListener(v -> startActivity(new Intent(this, Forget_Password.class)));
    }
}