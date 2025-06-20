package com.example.trackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnResendVerification, btnAdminLogin;
    private TextView btnGoToSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // ⭐ ADDED: Firestore instance

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        editor = preferences.edit();

        if (preferences.getBoolean("islogin", false)) {
            startActivity(new Intent(LoginActivity.this, Home_Page.class));
            finish();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // ⭐ INITIALIZE: Firestore

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignUp = findViewById(R.id.btnGoToSignUp);
        btnResendVerification = findViewById(R.id.btnResendVerification);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        progressBar = findViewById(R.id.progressBar);

        btnResendVerification.setVisibility(View.GONE);

        // ⭐ NEW: Check if there is temporary data from Signup to clear
        if (preferences.contains("temp_email")) {
            etEmail.setText(preferences.getString("temp_email", ""));
            // Clear temporary data after reading
            preferences.edit().remove("temp_email").remove("temp_fullName").apply();
        }


        btnLogin.setOnClickListener(v -> loginUser());
        btnGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
        btnResendVerification.setOnClickListener(v -> resendVerificationEmail());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnAdminLogin.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, Admin_Login_Activity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // ⭐ MODIFIED: Fetch user profile data before proceeding
                    fetchAndSaveUserProfile(user);
                } else {
                    mAuth.signOut();
                    Toast.makeText(this, "Please verify your email first", Toast.LENGTH_LONG).show();
                    btnResendVerification.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ⭐ NEW: Function to fetch user data (name/email) from Firestore and save to SharedPreferences
    private void fetchAndSaveUserProfile(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");

                        // Save FullName and Email to SharedPreferences for ProfileFragment use
                        editor.putString("profile_fullName", fullName);
                        editor.putString("profile_email", email);
                        editor.putBoolean("islogin", true);
                        editor.apply();

                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Home_Page.class));
                        finish();
                    } else {
                        Log.e("LoginActivity", "User profile data missing in Firestore.");
                        // Fallback: Use Auth email if Firestore profile is missing
                        editor.putString("profile_email", user.getEmail());
                        editor.putString("profile_fullName", "User"); // Default name
                        editor.putBoolean("islogin", true);
                        editor.apply();

                        Toast.makeText(this, "Login successful (using default profile)", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Home_Page.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", "Failed to fetch profile: " + e.getMessage());
                    Toast.makeText(this, "Login failed: Could not load profile data.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showForgotPasswordDialog() {
        // ... (Logic remains the same)
        final EditText resetEmail = new EditText(this);
        resetEmail.setHint("Enter your registered email");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We will send a password reset link to your email.")
                .setView(resetEmail)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = resetEmail.getText().toString().trim();
                    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Reset email sent. Check inbox.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}