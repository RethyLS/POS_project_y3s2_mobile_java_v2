package com.example.pos_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pos_project.R;
import com.example.pos_project.activity.SalesActivity;
import com.example.pos_project.api.ApiClient;
import com.example.pos_project.auth.AuthManager;
import com.example.pos_project.dto.ApiResponse;
import com.example.pos_project.dto.LoginRequest;
import com.example.pos_project.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etEmail, etPassword;
    private TextView btnLogin;
    private ProgressBar progressBar;
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_login);
            
            // Set status bar color to match login background (#f5f5f5)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(android.graphics.Color.parseColor("#f5f5f5"));
                // Make status bar icons dark since we're using light background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() | 
                        android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            
            authManager = AuthManager.getInstance(this);
            
            // Check if already logged in
            if (authManager.isLoggedIn()) {
                startMainActivity();
                return;
            }
            
            initViews();
            setupClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    
    private void initViews() {
        try {
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            progressBar = findViewById(R.id.progressBar);
            
            // Check if views are found
            if (etEmail == null || etPassword == null || btnLogin == null || progressBar == null) {
                Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            if (validateInput(email, password)) {
                login(email, password);
            }
        });
    }
    
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void login(String email, String password) {
        showProgress(true);
        
        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<ApiResponse<LoginResponse>> call = ApiClient.getApiService().login(loginRequest);
        
        call.enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                showProgress(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        LoginResponse loginResponse = apiResponse.getData();
                        
                        // Save auth token and user info
                        authManager.saveAuthToken(loginResponse.getToken());
                        if (loginResponse.getUser() != null) {
                            authManager.saveUserInfo(
                                loginResponse.getUser().getId(),
                                loginResponse.getUser().getEmail(),
                                loginResponse.getUser().getName()
                            );
                        }
                        
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            apiResponse.getMessage() != null ? apiResponse.getMessage() : "Login failed", 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }
    
    private void startMainActivity() {
        try {
            Intent intent = new Intent(this, SalesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to sales screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}