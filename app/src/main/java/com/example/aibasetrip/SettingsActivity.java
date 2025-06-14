package com.example.aibasetrip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotifications = findViewById(R.id.switchNotifications);
        btnLogout = findViewById(R.id.btnLogout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Load the saved notification preference
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isNotificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(isNotificationsEnabled);

        // Save the switch state when toggled
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
            Toast.makeText(this, "Notification setting updated", Toast.LENGTH_SHORT).show();
        });

        // Logout button functionality
        btnLogout.setOnClickListener(v -> {
            // Perform logout action here (clear user data, navigate to login screen)
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }
}
