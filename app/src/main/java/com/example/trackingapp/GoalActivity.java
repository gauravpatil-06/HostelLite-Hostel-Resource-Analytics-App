package com.example.trackingapp;
import com.example.trackingapp.R;  // ✅ Correct

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class GoalActivity extends AppCompatActivity {

    private EditText etWaterGoal, etElectricityGoal;
    private Button btnSaveGoal;
    private FirebaseFirestore firestore;
    private String userId = "user123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        etWaterGoal = findViewById(R.id.etWaterGoal);
        etElectricityGoal = findViewById(R.id.etElectricityGoal);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);

        firestore = FirebaseFirestore.getInstance();

        loadSavedGoals(); // Load previously saved goals

        btnSaveGoal.setOnClickListener(v -> saveGoalToFirestore());
    }

    private void saveGoalToFirestore() {
        String waterGoalStr = etWaterGoal.getText().toString().trim();
        String electricityGoalStr = etElectricityGoal.getText().toString().trim();

        if (waterGoalStr.isEmpty() || electricityGoalStr.isEmpty()) {
            Toast.makeText(this, "Please enter both goals!", Toast.LENGTH_SHORT).show();
            return;
        }

        int waterGoal = Integer.parseInt(waterGoalStr);
        int electricityGoal = Integer.parseInt(electricityGoalStr);

        // Save to Firestore
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("waterGoal", waterGoal);
        goalData.put("electricityGoal", electricityGoal);

        firestore.collection("users").document(userId).collection("goals")
                .document("data")
                .set(goalData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Goal saved successfully!", Toast.LENGTH_SHORT).show();
                    saveToLocalStorage(waterGoal, electricityGoal); // Save to SharedPreferences
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save goal!", Toast.LENGTH_SHORT).show());
    }

    private void saveToLocalStorage(int waterGoal, int electricityGoal) {
        SharedPreferences prefs = getSharedPreferences("UsageData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("waterGoal", waterGoal);
        editor.putInt("electricityGoal", electricityGoal);
        editor.apply();
    }

    private void loadSavedGoals() {
        SharedPreferences prefs = getSharedPreferences("UsageData", MODE_PRIVATE);
        int waterGoal = prefs.getInt("waterGoal", 0);
        int electricityGoal = prefs.getInt("electricityGoal", 0);

        if (waterGoal > 0) etWaterGoal.setText(String.valueOf(waterGoal));
        if (electricityGoal > 0) etElectricityGoal.setText(String.valueOf(electricityGoal));
    }
}
