package com.example.aibasetrip;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ProfileActivity extends AppCompatActivity {

    // Declare TextViews for displaying user details
    private TextView tvUserName, tvUserEmail, tvUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Initialize the TextViews
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);

        // Load user details from SharedPreferences
        loadUserDetails();
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Retrieve user details from SharedPreferences
        String userName = sharedPreferences.getString("username", "N/A");
        String userEmail = sharedPreferences.getString("email", "N/A");
        String userPhone = sharedPreferences.getString("user_phone", "N/A");

        // Set the user details to the TextViews
        tvUserName.setText("Name: " + userName);
        tvUserEmail.setText("Email: " + userEmail);
        tvUserPhone.setText("Phone: " + userPhone);
    }
}
