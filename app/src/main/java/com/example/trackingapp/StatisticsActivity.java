package com.example.trackingapp;
import com.example.trackingapp.R;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvWaterUsage, tvElectricityUsage, tvGoal;
    private FirebaseFirestore firestore;
    private String userId = "user123"; // Replace with actual user ID

    private long waterUsage = 0, electricityUsage = 0, waterGoal = 100, electricityGoal = 50;

    private PieChart waterChart, electricityChart; // Circular Charts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvWaterUsage = findViewById(R.id.tvWaterUsage);
        tvElectricityUsage = findViewById(R.id.tvElectricityUsage);
        tvGoal = findViewById(R.id.tvGoal);
        waterChart = findViewById(R.id.waterChart);
        electricityChart = findViewById(R.id.electricityChart);

        firestore = FirebaseFirestore.getInstance();

        fetchGoalData();   // Fetch goals first
        fetchUsageData();  // Fetch usage after goal
    }

    private void fetchGoalData() {
        DocumentReference docRef = firestore.collection("users").document(userId).collection("goals").document("data");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                waterGoal = document.getLong("waterGoal") != null ? document.getLong("waterGoal") : 100;
                electricityGoal = document.getLong("electricityGoal") != null ? document.getLong("electricityGoal") : 50;

                tvGoal.setText("Goal: Water " + waterGoal + "L, Electricity " + electricityGoal + " kWh");

                fetchUsageData(); // Fetch usage after goal
            } else {
                showError("No goal data found!");
            }
        });
    }

    private void fetchUsageData() {
        String todayDate = getCurrentDate();  // Helper method to get today's date in YYYY-MM-DD format
        DocumentReference docRef = firestore.collection("UsageData")
                .document(userId)
                .collection("daily")
                .document(todayDate);  // Fetch usage for today's date

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                waterUsage = document.getLong("waterUsed") != null ? document.getLong("waterUsed") : 0;
                electricityUsage = document.getLong("electricityUsed") != null ? document.getLong("electricityUsed") : 0;

                // Display the fetched data in the TextViews
                tvWaterUsage.setText("Water Usage: " + waterUsage + "L");
                tvElectricityUsage.setText("Electricity Usage: " + electricityUsage + " kWh");

                checkAndSendNotifications(); // Send notification if limit exceeded
                updateCharts(); // Update the charts
            } else {
                showError("No usage data found for today!");
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void checkAndSendNotifications() {
        if (waterUsage > waterGoal) {
            NotificationHelper.sendGoalExceededNotification(this, "Water usage exceeded! (" + waterUsage + "L)");
        }
        if (electricityUsage > electricityGoal) {
            NotificationHelper.sendGoalExceededNotification(this, "Electricity usage exceeded! (" + electricityUsage + " kWh)");
        }
    }

    private void updateCharts() {
        updateChart(waterChart, "Water Usage", waterUsage, waterGoal, Color.CYAN);
        updateChart(electricityChart, "Electricity Usage", electricityUsage, electricityGoal, Color.BLUE);
    }

    private void updateChart(PieChart chart, String label, long usage, long goal, int color) {
        List<PieEntry> entries = new ArrayList<>();

        float usedPercentage = Math.min((float) usage / goal * 100, 100); // max 100%
        float exceededPercentage = usage > goal ? ((float) (usage - goal) / goal * 100) : 0;
        float remainingPercentage = usage < goal ? (100 - usedPercentage) : 0;

        if (usedPercentage > 0) {
            entries.add(new PieEntry(usedPercentage, "Used"));
        }
        if (remainingPercentage > 0) {
            entries.add(new PieEntry(remainingPercentage, "Remaining"));
        }
        if (exceededPercentage > 0) {
            entries.add(new PieEntry(exceededPercentage, "Exceeded"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        if (usedPercentage > 0) colors.add(color); // Used = cyan/blue
        if (remainingPercentage > 0) colors.add(Color.LTGRAY); // Remaining = gray
        if (exceededPercentage > 0) colors.add(Color.RED); // Exceeded = red

        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(80f);
        chart.setCenterText(usage > goal
                ? "100% + Exceeded"
                : String.format("%.0f%%", usedPercentage));
        chart.setCenterTextSize(16f);
        chart.setEntryLabelTextSize(12f);
        chart.getLegend().setEnabled(false);

        chart.invalidate();
    }



    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("StatisticsActivity", message);
    }
}
