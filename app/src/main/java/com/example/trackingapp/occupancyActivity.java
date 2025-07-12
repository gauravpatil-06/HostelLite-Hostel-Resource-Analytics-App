package com.example.trackingapp;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class occupancyActivity extends AppCompatActivity {

    private TextInputEditText etSelectDate, etOccupancyCount;
    private Button btnSaveOccupancy;
    private TextView tvCurrentOccupancy;
    private BarChart barChartWeekly, barChartMonthly, barChartYearly;
    private NestedScrollView contentScrollView;
    private LinearLayout emptyStateLayout;
    private int previousOccupancy = 0;

    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();
    private final String OCCUPANCY_DATA_KEY = "occupancy_data";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_occupancy);
        setTitle("Hostel Occupancy");

        sharedPreferences = getSharedPreferences("HostelLitePrefs", Context.MODE_PRIVATE);
        initializeUI();
        setupEventListeners();
        loadDataOnStart();
    }

    private void initializeUI() {
        etSelectDate = findViewById(R.id.etSelectDate);
        etOccupancyCount = findViewById(R.id.etOccupancyCount);
        btnSaveOccupancy = findViewById(R.id.btnSaveOccupancy);
        tvCurrentOccupancy = findViewById(R.id.tvCurrentOccupancy);
        barChartWeekly = findViewById(R.id.barChartWeekly);
        barChartMonthly = findViewById(R.id.barChartMonthly);
        barChartYearly = findViewById(R.id.barChartYearly);
        contentScrollView = findViewById(R.id.contentScrollView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        etSelectDate.setText(sdf.format(new Date()));
    }

    private void setupEventListeners() {
        etSelectDate.setOnClickListener(v -> showDatePicker());
        btnSaveOccupancy.setOnClickListener(v -> saveAndRefreshData());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            etSelectDate.setText(sdf.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Hi method fakt activity start hotana data load karel
    private void loadDataOnStart() {
        Map<String, Integer> occupancyData = getOccupancyData();
        if (occupancyData.isEmpty()) {
            contentScrollView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvCurrentOccupancy.setText("0 Students");
            previousOccupancy = 0;
        } else {
            contentScrollView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            List<String> sortedDates = new ArrayList<>(occupancyData.keySet());
            Collections.sort(sortedDates, Collections.reverseOrder());
            Integer latestOccupancy = occupancyData.get(sortedDates.get(0));
            tvCurrentOccupancy.setText(latestOccupancy + " Students");
            previousOccupancy = latestOccupancy;
            updateAllCharts(occupancyData);
        }
    }

    // Save button dabalyavar ha corrected logic chalel
    private void saveAndRefreshData() {
        String date = etSelectDate.getText().toString();
        String countStr = etOccupancyCount.getText().toString();
        if (date.isEmpty() || countStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int newCount = Integer.parseInt(countStr);
            Map<String, Integer> occupancyData = getOccupancyData();
            occupancyData.put(date, newCount);
            String jsonString = gson.toJson(occupancyData);
            sharedPreferences.edit().putString(OCCUPANCY_DATA_KEY, jsonString).apply();
            Toast.makeText(this, "Occupancy for " + date + " saved!", Toast.LENGTH_SHORT).show();
            etOccupancyCount.setText("");

            // CORRECTED LOGIC: Direct enter kelela number animation sathi vapra
            animateCounter(previousOccupancy, newCount);
            previousOccupancy = newCount;

            // Navin data nusar sagale charts refresh kara
            updateAllCharts(getOccupancyData());

            if (contentScrollView.getVisibility() == View.GONE) {
                contentScrollView.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for students", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAllCharts(Map<String, Integer> occupancyData) {
        setupWeeklyChart(occupancyData);
        setupMonthlyChart(occupancyData);
        setupYearlyChart(occupancyData);
    }

    private void animateCounter(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> tvCurrentOccupancy.setText(animation.getAnimatedValue().toString() + " Students"));
        animator.start();
    }

    private Map<String, Integer> getOccupancyData() {
        String json = sharedPreferences.getString(OCCUPANCY_DATA_KEY, "{}");
        Type type = new TypeToken<LinkedHashMap<String, Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void setupWeeklyChart(Map<String, Integer> data) {
        if(data.isEmpty()){ barChartWeekly.clear(); barChartWeekly.invalidate(); return; }
        // ... (rest of the method is the same)
        List<String> sortedDates = new ArrayList<>(data.keySet()); Collections.sort(sortedDates, Collections.reverseOrder());
        List<String> labels = new ArrayList<>(); ArrayList<BarEntry> entries = new ArrayList<>(); SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        int limit = Math.min(7, sortedDates.size());
        for (int i = 0; i < limit; i++) {
            String dateStr = sortedDates.get(i);
            try { Date date = sdf.parse(dateStr); labels.add(dayMonthFormat.format(date)); } catch (ParseException e) { labels.add(dateStr.substring(5));}
            entries.add(new BarEntry(i, data.get(dateStr)));
        }
        Collections.reverse(labels); Collections.reverse(entries);
        BarDataSet dataSet = new BarDataSet(entries, "Occupancy"); dataSet.setColor(Color.parseColor("#5E35B1"));
        BarData barData = new BarData(dataSet); barData.setBarWidth(0.4f);
        barChartWeekly.setData(barData); styleBarChart(barChartWeekly, labels); barChartWeekly.invalidate();
    }

    private void setupMonthlyChart(Map<String, Integer> data) {
        if(data.isEmpty()){ barChartMonthly.clear(); barChartMonthly.invalidate(); return; }
        // ... (rest of the method is the same)
        Map<String, List<Integer>> monthlyAggregation = new LinkedHashMap<>(); SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        for(Map.Entry<String, Integer> entry : data.entrySet()){ try { Date date = sdf.parse(entry.getKey()); String monthKey = monthFormat.format(date); monthlyAggregation.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(entry.getValue()); } catch (ParseException e) { e.printStackTrace(); }}
        List<String> labels = new ArrayList<>(); ArrayList<BarEntry> entries = new ArrayList<>(); int i = 0;
        SimpleDateFormat monthYearDisplayFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        for(Map.Entry<String, List<Integer>> entry : monthlyAggregation.entrySet()){ double average = entry.getValue().stream().mapToInt(val -> val).average().orElse(0.0); entries.add(new BarEntry(i, (float) average)); try { Date monthDate = monthFormat.parse(entry.getKey()); labels.add(monthYearDisplayFormat.format(monthDate)); } catch (ParseException e) { labels.add(entry.getKey()); } i++; }
        BarDataSet dataSet = new BarDataSet(entries, "Average Occupancy per Month"); dataSet.setColor(Color.parseColor("#7E57C2"));
        BarData barData = new BarData(dataSet); barData.setBarWidth(0.4f);
        barChartMonthly.setData(barData); styleBarChart(barChartMonthly, labels); barChartMonthly.invalidate();
    }

    private void setupYearlyChart(Map<String, Integer> data) {
        if(data.isEmpty()){ barChartYearly.clear(); barChartYearly.invalidate(); return; }
        // ... (rest of the method is the same)
        Map<String, List<Integer>> yearlyAggregation = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> entry : data.entrySet()){ try { String yearKey = entry.getKey().substring(0, 4); yearlyAggregation.computeIfAbsent(yearKey, k -> new ArrayList<>()).add(entry.getValue()); } catch (Exception e) { e.printStackTrace(); }}
        List<String> labels = new ArrayList<>(yearlyAggregation.keySet()); Collections.sort(labels); ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i = 0; i < labels.size(); i++){ String year = labels.get(i); double average = yearlyAggregation.get(year).stream().mapToInt(val -> val).average().orElse(0.0); entries.add(new BarEntry(i, (float) average)); }
        BarDataSet dataSet = new BarDataSet(entries, "Average Occupancy per Year"); dataSet.setColor(Color.parseColor("#9575CD"));
        BarData barData = new BarData(dataSet);
        // FINAL CHANGE: Yearly chart chi width ajun kami keli
        barData.setBarWidth(0.2f);
        barChartYearly.setData(barData); styleBarChart(barChartYearly, labels); barChartYearly.invalidate();
    }

    private void styleBarChart(BarChart chart, List<String> labels) {
        int textColor = getThemeTextColor(); chart.getDescription().setEnabled(false); chart.getLegend().setEnabled(false); chart.getAxisRight().setEnabled(false); chart.setFitBars(true);
        chart.getAxisLeft().setTextColor(textColor); chart.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chart.getXAxis(); xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); xAxis.setGranularity(1f); xAxis.setTextColor(textColor); xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); xAxis.setDrawGridLines(false);
        chart.animateY(1200, Easing.EaseInOutCubic);
    }

    private int getThemeTextColor() {
        TypedValue typedValue = new TypedValue(); getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int color = arr.getColor(0, Color.BLACK); arr.recycle();
        return color;
    }
}