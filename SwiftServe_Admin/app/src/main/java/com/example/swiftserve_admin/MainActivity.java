package com.example.swiftserve_admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private MaterialButton loginButton;
    private TextInputEditText loginEmail, loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);

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
                            boolean found = false;

                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);

                                if (email.equals(user.getString("email")) &&
                                        password.equals(user.getString("password"))) {

                                    String userType = user.getString("usertype");
                                    if (!userType.equalsIgnoreCase("admin")) {
                                        Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // SAVE ALL DATA FOR EDIT PROFILE
                                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                                    editor.putBoolean("is_logged_in", true);
                                    editor.putString("user_type", userType);
                                    editor.putString("user_name", user.getString("username"));
                                    editor.putString("user_email", email);
                                    editor.putString("logged_in_student_id", "bsse2509244");
                                    editor.putString("logged_in_user_id", user.getString("username")); // USE _id
                                    editor.apply();

                                    found = true;
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, Dashboard.class));
                                    finish();
                                    break;
                                }
                            }

                            if (!found) {
                                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            );

            queue.add(request);
        });
    }
}