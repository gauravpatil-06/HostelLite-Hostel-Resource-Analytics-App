package com.example.trackingapp;
import com.example.trackingapp.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UsageActivity extends AppCompatActivity {

    private EditText etWaterUsage, etElectricityUsage;
    private Button btnSubmit;
    private FirebaseFirestore firestore;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;
    private String userId = "user123"; // Replace with actual user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        etWaterUsage = findViewById(R.id.etWaterUsage);
        etElectricityUsage = findViewById(R.id.etElectricityUsage);
        btnSubmit = findViewById(R.id.btnSubmit);
        firestore = FirebaseFirestore.getInstance();

        NotificationHelper.createNotificationChannel(this);
        checkNotificationPermission();

        btnSubmit.setOnClickListener(v -> {
            String waterInput = etWaterUsage.getText().toString().trim();
            String electricityInput = etElectricityUsage.getText().toString().trim();

            if (waterInput.isEmpty() || electricityInput.isEmpty()) {
                Toast.makeText(this, "Please enter both water and electricity usage!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float waterUsed = Float.parseFloat(waterInput);
                float electricityUsed = Float.parseFloat(electricityInput);

                // ✅ Save new usage data to Firestore under daily subcollection with today's date
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                Map<String, Object> usageData = new HashMap<>();
                usageData.put("date", todayDate); // Important: also save date
                usageData.put("waterUsed", waterUsed);
                usageData.put("electricityUsed", electricityUsed);

                firestore.collection("UsageData")
                        .document(userId)
                        .collection("daily")
                        .document(todayDate)
                        .set(usageData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Usage data saved successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save usage data!", Toast.LENGTH_SHORT).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input! Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Notification permission denied! Alerts may not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
