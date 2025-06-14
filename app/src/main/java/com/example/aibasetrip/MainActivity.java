package com.example.aibasetrip;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;

import org.jetbrains.annotations.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    Button btnlogin;
    Button loginWithGoogleBtn, signupWithGoogleBtn;
    EditText emailInput, passwordInput;
    Button loginBtn;
    DBHelper dbHelper;
    boolean isSignUpFlow = false;
TextView registertext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this is your layout file name
        dbHelper = new DBHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        btnlogin = findViewById(R.id.loginBtn);
        registertext = findViewById(R.id.registerText);
        registertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,register.class);
                startActivity(intent);
            }
        });


        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean login = dbHelper.checkLogin(email, password);
                if (login) {

                    String name = dbHelper.getUserName(email, password);
                    // Save username and email in SharedPreferences
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("username", name)  // Save username
                            .putString("email", email)    // Save email
                            .apply();




                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    intent.putExtra("user_email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();

        // Google SignIn Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build();

        // GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Find buttons
        loginWithGoogleBtn = findViewById(R.id.loginWithGoogleBtn);
//        signupWithGoogleBtn = findViewById(R.id.signupWithGoogleBtn);

        // Google Sign In
        // flag to track which button was used
        loginWithGoogleBtn.setOnClickListener(view -> {
            isSignUpFlow = false;
            signInWithGoogle();
        });



    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode(), e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String name = user.getDisplayName();
                        String email = user.getEmail();

                        if (isSignUpFlow) {
                            // Insert user into SQLite
                            boolean inserted = dbHelper.insertUser(name, email, ""); // Password can be blank or a fixed value
                            if (inserted) {
                                Toast.makeText(MainActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "User already exists.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Save in SharedPreferences
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("username", name)
                                .putString("email", email)
                                .apply();

                        // Redirect
                        Toast.makeText(MainActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Dashboard.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}