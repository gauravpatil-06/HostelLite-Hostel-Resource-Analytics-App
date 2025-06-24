package com.example.trackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.trackingapp.R;



public class MainActivity extends AppCompatActivity {

    private TextView waterUsageTextView, electricityUsageTextView;
    private FirebaseFirestore db;
    private Button btnStatistics, btnDailyUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        waterUsageTextView = findViewById(R.id.waterUsageTextView);
        electricityUsageTextView = findViewById(R.id.electricityUsageTextView);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnDailyUsage = findViewById(R.id.btnDailyUsage);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Fetch real-time usage data
        fetchUsageData();

        // Open Statistics page
        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        // Open Daily Usage page
        btnDailyUsage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailyUsageActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUsageData() {
        DocumentReference docRef = db.collection("users").document("usage");

        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Long waterUsage = documentSnapshot.getLong("waterUsage");
                Long electricityUsage = documentSnapshot.getLong("electricityUsage");

                String waterText = "Water Usage: " + (waterUsage != null ? waterUsage + " liters" : "N/A");
                String electricityText = "Electricity Usage: " + (electricityUsage != null ? electricityUsage + " kWh" : "N/A");

                waterUsageTextView.setText(waterText);
                electricityUsageTextView.setText(electricityText);
            } else {
                Toast.makeText(MainActivity.this, "No real-time data available!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
