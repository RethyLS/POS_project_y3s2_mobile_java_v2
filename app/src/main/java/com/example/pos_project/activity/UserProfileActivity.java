package com.example.pos_project.activity;

import com.example.pos_project.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pos_project.auth.AuthManager;

public class UserProfileActivity extends AppCompatActivity {

    private TextView passwordText;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set window to 70% width and slide from left
        getWindow().setLayout(
            (int) (getResources().getDisplayMetrics().widthPixels * 0.7), 
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        getWindow().setGravity(Gravity.LEFT | Gravity.TOP);
        
        // Adjust the dim amount for the background (30% of the screen)
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.3f; // 0.3 = 30% dark (adjust between 0.0 - 1.0)
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        
        // Force slide animation from left
        overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set status bar color to match sales activity
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.background, getTheme()));
            // Make status bar icons dark since we're using light background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // Clear any existing flags and set light status bar
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }

        // Initialize views
        passwordText = findViewById(R.id.password_edit);

        // Load user data
        AuthManager authManager = AuthManager.getInstance(this);
        TextView usernameText = findViewById(R.id.username_text);
        TextView emailText = findViewById(R.id.email_edit);

        // Set real user data
        String username = authManager.getUsername();
        if (username != null && !username.isEmpty()) {
            usernameText.setText(username);
        }

        String email = authManager.getUserEmail();
        if (email != null && !email.isEmpty()) {
            emailText.setText(email);
        }

        // Password field - show placeholder since we don't store actual password
        passwordText.setText("••••••••");

        // Set up logout button
        TextView logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        // Close activity when touched outside the content area
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            // Check if touch is outside the content area
            View contentView = findViewById(R.id.content_container);
            if (contentView != null) {
                int[] location = new int[2];
                contentView.getLocationOnScreen(location);
                float x = event.getRawX();
                float y = event.getRawY();

                // If touch is outside the content container bounds, close the activity
                if (x < location[0] || x > location[0] + contentView.getWidth() ||
                    y < location[1] || y > location[1] + contentView.getHeight()) {
                    finish();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void finish() {
        super.finish();
        // Slide out to the LEFT when closing
        overridePendingTransition(0, R.anim.slide_out_left);
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