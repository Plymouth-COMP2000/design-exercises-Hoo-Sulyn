package com.example.swiftserve_admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;

public class Forget_Password extends AppCompatActivity {

    private TextInputEditText etEmail, etNewPassword, etConfirmPassword;
    private TextInputLayout layoutNewPass, layoutConfirmPass;
    private MaterialButton btnAction;
    private TextView backButton;

    private boolean isEmailVerified = false;
    private String studentId = "bsse2509244";
    private User matchedUser; // To hold all existing details for the update
    private String matchedUserId; // Needed for the API path
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        userService = new UserService(this);

        etEmail = findViewById(R.id.email_address);
        etNewPassword = findViewById(R.id.new_password);
        etConfirmPassword = findViewById(R.id.confirm_password);

        // Find layouts to toggle visibility
        layoutNewPass = findViewById(R.id.layout_new_password);
        layoutConfirmPass = findViewById(R.id.layout_confirm_password);
        btnAction = findViewById(R.id.btn_action);
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> finish());

        btnAction.setOnClickListener(v -> {
            if (!isEmailVerified) {
                verifyEmail();
            } else {
                performPasswordUpdate();
            }
        });
    }

    private void verifyEmail() {
        String email = etEmail.getText().toString().trim();
        String url = "http://10.240.72.69/comp2000/coursework/read_all_users/" + studentId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray users = response.getJSONArray("users");
                        boolean found = false;

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject obj = users.getJSONObject(i);
                            if (obj.getString("email").equalsIgnoreCase(email)) {
                                if (obj.getString("usertype").equalsIgnoreCase("admin")) {
                                    Toast.makeText(this, "Admin reset not allowed here", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Store user info so we don't lose other fields (like username/type) [cite: 117-124]
                                matchedUserId = obj.getString("username");
                                matchedUser = new User(
                                        obj.getString("username"),
                                        obj.getString("password"),
                                        obj.getString("firstname"),
                                        obj.getString("lastname"),
                                        obj.getString("email"),
                                        obj.getString("contact"),
                                        obj.getString("usertype")
                                );

                                showPasswordFields();
                                found = true;
                                break;
                            }
                        }
                        if (!found) Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showPasswordFields() {
        isEmailVerified = true;
        etEmail.setEnabled(false);
        layoutNewPass.setVisibility(View.VISIBLE);
        layoutConfirmPass.setVisibility(View.VISIBLE);
        btnAction.setText("Set Password");
    }

    private void performPasswordUpdate() {
        String newPass = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (newPass.isEmpty() || !newPass.equals(confirm)) {
            Toast.makeText(this, "Passwords must match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update only the password field in our object
        matchedUser.setPassword(newPass);

        // Call our UserService to send the PUT request [cite: 106]
        userService.updateUserProfile(studentId, matchedUserId, matchedUser, new UserService.UserProfileListener() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(Forget_Password.this, "Password Updated Successfully!", Toast.LENGTH_LONG).show();
                finish(); // Close page and go back to Login
            }

            @Override
            public void onError(String message) {
                Toast.makeText(Forget_Password.this, "Update Failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}