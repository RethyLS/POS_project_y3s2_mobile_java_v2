package com.example.pos_project.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF_NAME = "pos_auth";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USERNAME = "username";
    
    private SharedPreferences preferences;
    private static AuthManager instance;
    
    private AuthManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void saveAuthToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }
    
    public String getAuthToken() {
        return preferences.getString(KEY_TOKEN, null);
    }
    
    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }
    
    public void logout() {
        preferences.edit().clear().apply();
    }
    
    public String getAuthHeader() {
        String token = getAuthToken();
        return token != null ? "Bearer " + token : null;
    }
    
    public void saveUserInfo(int userId, String email, String name, String username) {
        preferences.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USERNAME, username)
                .apply();
    }
    
    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }
    
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }
    
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }
    
    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }
    
    public void clearUserData() {
        preferences.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .remove(KEY_USERNAME)
                .apply();
    }
}