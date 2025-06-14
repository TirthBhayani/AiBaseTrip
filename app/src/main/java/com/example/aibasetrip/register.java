package com.example.aibasetrip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class register extends AppCompatActivity {
TextView AlreadyAccount;
    EditText emailInput, passwordInput, nameInput, cpassInput;
    Button registerBtn;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        AlreadyAccount = findViewById(R.id.Alredyacoount);
        AlreadyAccount.setOnClickListener(v ->
        {
            Intent intent = new Intent(register.this,MainActivity.class);
            startActivity(intent);
        });

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);
        nameInput = findViewById(R.id.nameInput);
        cpassInput = findViewById(R.id.cpassInput);
        dbHelper = new DBHelper(this);

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String cpass = cpassInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }
            if (!password.equals(cpass)) {
                cpassInput.setError("Passwords do not match");
                return;
            }

            boolean success = dbHelper.insertUser(name, email, password);
            if (success) {
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("username", name)
                        .apply();
                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(register.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            }
        });

    }
}