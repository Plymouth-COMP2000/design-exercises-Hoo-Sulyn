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

    public void createUser(String studentId, User user, UserProfileListener listener) {
        String url = BASE_URL + "/create_user/" + studentId; // URL as per doc

        JSONObject postData = new JSONObject();
        try {
            postData.put("username", user.getUsername());
            postData.put("password", user.getPassword());
            postData.put("firstname", user.getFirstname());
            postData.put("lastname", user.getLastname());
            postData.put("email", user.getEmail());
            postData.put("contact", user.getContact());
            postData.put("usertype", user.getUsertype()); // Usually "guest"
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> listener.onSuccess(user),
                error -> listener.onError("Registration Error: " + error.getMessage())
        );

        requestQueue.add(request);
    }

    // ==========================================================
    // 2. UPDATE PROFILE (PUT) - Endpoint assumed to be /update_user
    // ==========================================================
    public void updateUserProfile(String studentId, String userId, User user, UserProfileListener listener) {
        // 1. Ensure the URL matches the documentation
        String url = BASE_URL + "/update_user/" + studentId + "/" + userId;

        JSONObject postData = new JSONObject();
        try {
            // 2. The documentation requires ALL these fields for a successful update [cite: 117-124]
            postData.put("username", user.getUsername());
            postData.put("password", user.getPassword());
            postData.put("firstname", user.getFirstname());
            postData.put("lastname", user.getLastname());
            postData.put("email", user.getEmail());
            postData.put("contact", user.getContact());
            postData.put("usertype", user.getUsertype());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 3. Method MUST be PUT as per documentation
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, postData,
                response -> {
                    // Documentation returns "User updated successfully" [cite: 141]
                    listener.onSuccess(user);
                },
                error -> listener.onError("Update failed: " + error.getMessage())
        );

        requestQueue.add(request);
    }

    public void deleteUser(String studentId, String userId, final UserProfileListener listener) {
        // URL: http://10.240.72.69/comp2000/coursework/delete_user/bsse2509244/username
        String url = BASE_URL + "/delete_user/" + studentId + "/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> listener.onSuccess(null),
                error -> listener.onError(error.getMessage() != null ? error.getMessage() : "Unknown Error")
        );

        requestQueue.add(request);
    }
}