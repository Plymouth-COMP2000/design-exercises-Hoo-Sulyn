package com.example.swiftserve_admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;


public class Register_Page extends AppCompatActivity {
    private TextInputEditText firstName, lastName, email, phone, password, confirmPassword;
    private MaterialButton registerButton, loginOption;
    private MaterialCheckBox privacyCheck, termsCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.loginEmail);
        phone = findViewById(R.id.phoneNum);
        password = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerButton);
        privacyCheck = findViewById(R.id.privacy_policy);
        termsCheck = findViewById(R.id.tnc);
        loginOption = findViewById(R.id.loginOption);

        loginOption.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        registerButton.setOnClickListener(v -> {

            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String phoneNum = phone.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (fName.isEmpty() || lName.isEmpty() || userEmail.isEmpty()
                    || phoneNum.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {

                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!privacyCheck.isChecked() || !termsCheck.isChecked()) {
                Toast.makeText(
                        this,
                        "You must agree to Privacy Policy and Terms & Conditions",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(this);
            String checkUrl = "http://10.240.72.69/comp2000/coursework/read_all_users/bsse2509244";

            JsonObjectRequest checkRequest = new JsonObjectRequest(
                    Request.Method.GET, checkUrl, null,
                    response -> {
                        try {
                            JSONArray users = response.getJSONArray("users");

                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);

                                if (userEmail.equalsIgnoreCase(user.getString("email"))) {
                                    Toast.makeText(
                                            this,
                                            "Email already exists. Please login instead.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    startActivity(new Intent(Register_Page.this, MainActivity.class));
                                    finish();
                                    return;
                                }

                            }

                            createUser(queue, fName, lName, userEmail, phoneNum, pass);

                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            );

            queue.add(checkRequest);
        });

    }

    private void createUser(RequestQueue queue,
                            String fName,
                            String lName,
                            String email,
                            String phone,
                            String password) {

        String url = "http://10.240.72.69/comp2000/coursework/create_user/bsse2509244";

        JSONObject body = new JSONObject();
        try {
            body.put("username", fName + " " + lName);
            body.put("email", email);
            body.put("password", password);
            body.put("phone", phone);
            body.put("usertype", "user");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(this, "Registration successful. Please login.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                },
                error -> Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

}