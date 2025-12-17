package com.example.swiftserve_admin;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class UserService {

    // BASE_URL taken from the API document and context
    private static final String BASE_URL = "http://10.240.72.69/comp2000/coursework";
    private final RequestQueue requestQueue;

    public UserService(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public interface UserProfileListener {
        void onSuccess(User user);
        void onError(String message);
    }

    // ==========================================================
    // 1. FETCH PROFILE (GET) - Using the specified /read_user endpoint
    // Endpoint: /read_user/{studentId}/{userId}
    // ==========================================================
    public void getUserProfile(String studentId, String userId, final UserProfileListener listener) {
        String url = BASE_URL + "/read_user/" + studentId + "/" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // FIX: Get the nested "user" object first
                        JSONObject userObject = response.getJSONObject("user");

                        User user = new User(
                                userObject.getString("username"),
                                userObject.getString("password"),
                                userObject.getString("firstname"),
                                userObject.getString("lastname"),
                                userObject.getString("email"),
                                userObject.getString("contact"),
                                userObject.getString("usertype")
                        );
                        listener.onSuccess(user);

                    } catch (JSONException e) {
                        listener.onError("Failed to parse user data: " + e.getMessage());
                    }
                },
                error -> listener.onError("Network error: " + error.getMessage())
        );

        requestQueue.add(jsonObjectRequest);
    }

    // ==========================================================
    // 2. UPDATE PROFILE (PUT) - Endpoint assumed to be /update_user
    // ==========================================================
    public void updateUserProfile(String studentId, String userId, final User user, final UserProfileListener listener) {
        String url = BASE_URL + "/update_user/" + studentId + "/" + userId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", user.getUsername());
            jsonBody.put("password", user.getPassword());
            jsonBody.put("firstname", user.getFirstname());
            jsonBody.put("lastname", user.getLastname());
            jsonBody.put("email", user.getEmail());
            jsonBody.put("contact", user.getContact());
            jsonBody.put("usertype", user.getUsertype());
        } catch (JSONException e) {
            listener.onError("Error creating request body: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> listener.onSuccess(user),
                error -> listener.onError("Network error updating profile: " + (error.getMessage() != null ? error.getMessage() : "Unknown Error"))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}