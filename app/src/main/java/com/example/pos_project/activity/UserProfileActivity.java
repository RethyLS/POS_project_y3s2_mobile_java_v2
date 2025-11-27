package com.example.pos_project.activity;

import com.example.pos_project.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pos_project.auth.AuthManager;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set up toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);  // Disable default title
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set status bar color to white with dark icons
        getWindow().setStatusBarColor(getResources().getColor(R.color.background));
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Initialize views
        AuthManager authManager = AuthManager.getInstance(this);
        TextView usernameText = findViewById(R.id.username_text);
        TextView emailText = findViewById(R.id.email_text);

        // Set user data
        String username = authManager.getUsername();
        if (username != null && !username.isEmpty()) {
            usernameText.setText(username);
        }

        String email = authManager.getUserEmail();
        if (email != null && !email.isEmpty()) {
            emailText.setText(email);
        }

        // Set up click listeners
        findViewById(R.id.language_item).setOnClickListener(v -> {
            Toast.makeText(this, "Language selection", Toast.LENGTH_SHORT).show();
            // TODO: Show language selection dialog
        });

        findViewById(R.id.email_item).setOnClickListener(v -> {
            Toast.makeText(this, "View user info", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to user info screen
        });

        findViewById(R.id.password_item).setOnClickListener(v -> {
            Toast.makeText(this, "Change password", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to change password screen
        });

        findViewById(R.id.privacy_policy_item).setOnClickListener(v -> {
            Toast.makeText(this, "Privacy policy", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to privacy policy screen
        });

        findViewById(R.id.terms_item).setOnClickListener(v -> {
            Toast.makeText(this, "Terms & Conditions", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to terms screen
        });

        // Set up logout button
        findViewById(R.id.logout_button).setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = 
            new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme);
        androidx.appcompat.app.AlertDialog dialog = builder
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (d, which) -> logout())
            .setNegativeButton("Cancel", null)
            .create();

        dialog.show();

        // Set dialog width to 80% of screen width
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
            android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            layoutParams.width = (int) (displayMetrics.widthPixels * 0.8);
            window.setAttributes(layoutParams);
        }
    }

    private void logout() {
        AuthManager authManager = AuthManager.getInstance(this);
        authManager.logout();

        Intent intent = new Intent(this, com.example.pos_project.activity.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}