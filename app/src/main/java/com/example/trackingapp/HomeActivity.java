package com.example.trackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.trackingapp.R;  // ✅ Correct


public class HomeActivity extends AppCompatActivity {

    private Button btnTrackUsage, btnSetGoals, btnViewStatistics, btnNudges, btnLogout ,btnDailyUsage;
    private TextView welcomeText;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI elements
        btnDailyUsage = findViewById(R.id.btnDailyUsage);
        btnDailyUsage.setOnClickListener(v -> startActivity(new Intent(this, DailyUsageActivity.class)));
        btnTrackUsage = findViewById(R.id.btnTrackUsage);
        btnSetGoals = findViewById(R.id.btnSetGoals);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);
        btnNudges = findViewById(R.id.btnNudges);
        btnLogout = findViewById(R.id.btnLogout);
        welcomeText = findViewById(R.id.welcomeText);

        // Retrieve user name from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName = prefs.getString("userName", "User"); // Default: "User"

        // Set personalized welcome message
        welcomeText.setText("Welcome, " + userName + "!");

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Button click listeners
        btnTrackUsage.setOnClickListener(v -> startActivity(new Intent(this, UsageActivity.class)));
        btnSetGoals.setOnClickListener(v -> startActivity(new Intent(this, GoalActivity.class)));
        btnViewStatistics.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnNudges.setOnClickListener(v -> startActivity(new Intent(this, NudgesActivity.class)));

        // Logout functionality
        btnLogout.setOnClickListener(v -> {
            // Clear SharedPreferences and redirect to Login
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendGoalExceededNotification("You've exceeded your goal!");
            } else {
                Toast.makeText(this, "Permission denied. Cannot send notifications.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to send notifications
    public void sendGoalExceededNotification(String message) {
        NotificationHelper.sendGoalExceededNotification(this, message);
    }
}

