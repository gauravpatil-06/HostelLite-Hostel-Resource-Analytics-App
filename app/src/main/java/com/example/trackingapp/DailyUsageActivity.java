package com.example.trackingapp;
import com.example.trackingapp.R;  // ✅ Correct

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DailyUsageActivity extends AppCompatActivity {

    private LineChart lineChart;
    private FirebaseFirestore firestore;
    private String userId = "user123"; // your user ID
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ArrayList<String> dateLabels = new ArrayList<>();
    private Map<String, Integer> waterData = new HashMap<>();
    private Map<String, Integer> electricityData = new HashMap<>();
    private long launchDateMillis;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailyusage);

        lineChart = findViewById(R.id.lineChart);
        refreshButton = findViewById(R.id.refreshButton);
        firestore = FirebaseFirestore.getInstance();

        try {
            Date launchDate = sdf.parse("2025-02-10"); // App Launch Date
            launchDateMillis = launchDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        fetchUsageData();

        refreshButton.setOnClickListener(v -> fetchUsageData());
    }

    private void fetchUsageData() {
        CollectionReference usageRef = firestore.collection("UsageData").document(userId).collection("daily");

        usageRef.get().addOnSuccessListener(querySnapshot -> {
            waterData.clear();
            electricityData.clear();

            for (QueryDocumentSnapshot document : querySnapshot) {
                String dateStr = document.getString("date");
                Number water = document.getLong("waterUsed");
                Number electricity = document.getLong("electricityUsed");

                if (dateStr != null && water != null && electricity != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        waterData.put(dateStr, waterData.getOrDefault(dateStr, 0) + water.intValue());
                        electricityData.put(dateStr, electricityData.getOrDefault(dateStr, 0) + electricity.intValue());
                    }
                }
            }

            buildCompleteDateList();
            setupLineChart();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load usage data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void buildCompleteDateList() {
        dateLabels.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(launchDateMillis);

        Calendar today = Calendar.getInstance();

        while (!calendar.after(today)) {
            dateLabels.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void setupLineChart() {
        ArrayList<Entry> waterEntries = new ArrayList<>();
        ArrayList<Entry> electricityEntries = new ArrayList<>();

        for (int i = 0; i < dateLabels.size(); i++) {
            String date = dateLabels.get(i);
            int water = 0;
            int electricity = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                water = waterData.getOrDefault(date, 0);
                electricity = electricityData.getOrDefault(date, 0);
            }

            waterEntries.add(new Entry(i, water));
            electricityEntries.add(new Entry(i, electricity));
        }

        LineDataSet waterDataSet = new LineDataSet(waterEntries, "Water Usage (L)");
        waterDataSet.setColor(Color.BLUE);
        waterDataSet.setCircleColor(Color.BLUE);
        waterDataSet.setLineWidth(2f);
        waterDataSet.setValueTextSize(0f);
        waterDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet electricityDataSet = new LineDataSet(electricityEntries, "Electricity Usage (kWh)");
        electricityDataSet.setColor(Color.rgb(255, 165, 0)); // Orange
        electricityDataSet.setCircleColor(Color.rgb(255, 165, 0));
        electricityDataSet.setLineWidth(2f);
        electricityDataSet.setValueTextSize(0f);
        electricityDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(waterDataSet, electricityDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels) {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dateLabels.size()) {
                    return dateLabels.get(index).substring(5); // MM-DD
                } else {
                    return "";
                }
            }
        });
        xAxis.setTextSize(12f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(14f);

        // Move legend below the chart with spacing
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false); // place outside chart
        legend.setYOffset(10f); // add vertical spacing

        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);

        lineChart.setTouchEnabled(true);
        lineChart.setHighlightPerTapEnabled(true);

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                if (index >= 0 && index < dateLabels.size()) {
                    String date = dateLabels.get(index);
                    Toast.makeText(DailyUsageActivity.this, "Date: " + date, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });

        lineChart.invalidate(); // Refresh chart
    }
}
