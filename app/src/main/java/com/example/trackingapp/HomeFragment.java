package com.example.trackingapp;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private View view;
    private TextInputEditText etWaterUsage, etElectricityUsage, etWaterGoal, etElectricityGoal, etSelectDate, etSelectTime;
    private PieChart waterChart, electricityChart;
    private BarChart combinedBarChart;
    private UsageViewModel usageViewModel;
    private TextView tvAlerts, tvWaterPercent, tvElectricityPercent;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d (EEE)", Locale.getDefault());

    private String selectedDate;
    private float lastWaterPercent = 0;
    private float lastElecPercent = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        try {
            usageViewModel = new ViewModelProvider(requireActivity()).get(UsageViewModel.class);
            initializeUI(view);
            setupEventListeners();
            observeViewModel();
        } catch (Exception e) {
            Log.e(TAG, "Error during view initialization: ", e);
            Toast.makeText(getContext(), "An error occurred.", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void initializeUI(View view) {
        etWaterUsage = view.findViewById(R.id.etWaterUsage);
        etElectricityUsage = view.findViewById(R.id.etElectricityUsage);
        etWaterGoal = view.findViewById(R.id.etWaterGoal);
        etElectricityGoal = view.findViewById(R.id.etElectricityGoal);
        etSelectDate = view.findViewById(R.id.etSelectDate);
        etSelectTime = view.findViewById(R.id.etSelectTime);
        waterChart = view.findViewById(R.id.waterChart);
        electricityChart = view.findViewById(R.id.electricityChart);
        combinedBarChart = view.findViewById(R.id.combinedBarChart);
        tvAlerts = view.findViewById(R.id.tvAlerts);
        tvWaterPercent = view.findViewById(R.id.tvWaterPercent);
        tvElectricityPercent = view.findViewById(R.id.tvElectricityPercent);

        etSelectTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));

        etWaterGoal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etElectricityGoal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etWaterUsage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        etElectricityUsage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
    }

    private void setupEventListeners() {
        view.findViewById(R.id.btnSubmit).setOnClickListener(v -> saveUsageData());
        view.findViewById(R.id.btnSaveGoal).setOnClickListener(v -> saveGoals());
        etSelectDate.setOnClickListener(v -> showDatePicker());
        etSelectTime.setOnClickListener(v -> showTimePicker());

        // Refresh button listener
        Button refreshButton = view.findViewById(R.id.refreshButton);
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> {
                if (usageViewModel != null) {
                    Toast.makeText(getContext(), "Refreshing data...", Toast.LENGTH_SHORT).show();
                    // Just calling fetch is enough, the observer will handle the animation
                    usageViewModel.fetchData();
                }
            });
        }
    }

    private void observeViewModel() {
        usageViewModel.idealWaterGoal.observe(getViewLifecycleOwner(), goal -> {
            if (etWaterGoal != null) {
                if (goal != null && goal > 0) etWaterGoal.setText(String.valueOf(goal));
                else etWaterGoal.setText("");
            }
            updateDashboardUI();
        });
        usageViewModel.idealElectricityGoal.observe(getViewLifecycleOwner(), goal -> {
            if (etElectricityGoal != null) {
                if (goal != null && goal > 0) etElectricityGoal.setText(String.valueOf(goal));
                else etElectricityGoal.setText("");
            }
            updateDashboardUI();
        });

        usageViewModel.allWaterData.observe(getViewLifecycleOwner(), waterData -> updateDashboardUI());
        usageViewModel.allElectricityData.observe(getViewLifecycleOwner(), elecData -> updateDashboardUI());
        usageViewModel.selectedDate.observe(getViewLifecycleOwner(), date -> updateDashboardUI());

        usageViewModel.weeklyWaterData.observe(getViewLifecycleOwner(), data -> setupCombinedBarChart());
        usageViewModel.weeklyElectricityData.observe(getViewLifecycleOwner(), data -> setupCombinedBarChart());
    }

    private void updateDashboardUI() {
        if (!isAdded() || usageViewModel == null) return;

        Map<String, Long> waterMap = usageViewModel.allWaterData.getValue();
        Map<String, Long> elecMap = usageViewModel.allElectricityData.getValue();
        String currentDate = usageViewModel.selectedDate.getValue();

        if (waterMap == null || elecMap == null || currentDate == null) return;

        selectedDate = currentDate;
        etSelectDate.setText(currentDate);

        Long waterUsed = waterMap.getOrDefault(currentDate, 0L);
        Long elecUsed = elecMap.getOrDefault(currentDate, 0L);

        updatePieCharts(waterUsed, elecUsed);
        updateAlertsAndPercentages(waterUsed, elecUsed);
    }

    private void updateAlertsAndPercentages(Long waterUsed, Long elecUsed) {
        Long idealWaterGoal = usageViewModel.idealWaterGoal.getValue();
        Long idealElecGoal = usageViewModel.idealElectricityGoal.getValue();

        float waterPercent = 0f;
        if (waterUsed != null && idealWaterGoal != null && idealWaterGoal > 0) {
            waterPercent = ((float) waterUsed / idealWaterGoal) * 100f;
        }
        if (tvWaterPercent != null) {
            animatePercentage(tvWaterPercent, lastWaterPercent, waterPercent);
            lastWaterPercent = waterPercent;
        }

        float elecPercent = 0f;
        if (elecUsed != null && idealElecGoal != null && idealElecGoal > 0) {
            elecPercent = ((float) elecUsed / idealElecGoal) * 100f;
        }
        if (tvElectricityPercent != null) {
            animatePercentage(tvElectricityPercent, lastElecPercent, elecPercent);
            lastElecPercent = elecPercent;
        }
    }

    private void updatePieCharts(Long waterUsed, Long elecUsed) {
        Long waterGoal = usageViewModel.idealWaterGoal.getValue();
        Long elecGoal = usageViewModel.idealElectricityGoal.getValue();
        setupPieChart(waterChart, waterUsed, waterGoal, Color.parseColor("#00BCD4"), Color.parseColor("#E0F7FA"), Color.parseColor("#00838F"));
        setupPieChart(electricityChart, elecUsed, elecGoal, Color.parseColor("#2196F3"), Color.parseColor("#E3F2FD"), Color.parseColor("#1976D2"));
    }

    private void saveGoals() {
        try {
            String waterStr = etWaterGoal.getText().toString().trim();
            String elecStr = etElectricityGoal.getText().toString().trim();
            if (waterStr.isEmpty() || elecStr.isEmpty()) {
                Toast.makeText(getContext(), "Please set both goals.", Toast.LENGTH_SHORT).show();
                return;
            }
            usageViewModel.saveGoals(Long.parseLong(waterStr), Long.parseLong(elecStr));
            Toast.makeText(getContext(), "Goals have been saved!", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers for goals.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUsageData() {
        String waterInput = etWaterUsage.getText().toString().trim();
        String elecInput = etElectricityUsage.getText().toString().trim();

        long waterLong = waterInput.isEmpty() ? 0 : Long.parseLong(waterInput);
        long elecLong = elecInput.isEmpty() ? 0 : Long.parseLong(elecInput);

        try {
            Map<String, Object> usageData = new HashMap<>();
            usageData.put("date", etSelectDate.getText().toString());
            usageData.put("time", etSelectTime.getText().toString());
            usageData.put("waterUsed", waterLong);
            usageData.put("electricityUsed", elecLong);
            usageViewModel.saveUsageData(usageData);
            Toast.makeText(getContext(), "Usage data saved!", Toast.LENGTH_SHORT).show();

            etWaterUsage.setText("");
            etElectricityUsage.setText("");
            etWaterUsage.clearFocus();
            etElectricityUsage.clearFocus();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPieChart(PieChart chart, Long used, Long goal, int usedColor, int remainingColor, int centerTextColor) {
        if (chart == null) return;
        if (used == null || goal == null || goal == 0) {
            chart.clear();
            chart.setCenterText("No Data");
            chart.setCenterTextColor(getThemeTextColor());
            chart.invalidate();
            return;
        }
        float rawPercent = ((float) used / goal) * 100f;
        float chartPercent = Math.min(100f, rawPercent);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(chartPercent, "Used"));
        if (chartPercent < 100) {
            entries.add(new PieEntry(100f - chartPercent, "Remaining"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(usedColor, remainingColor);
        dataSet.setValueTextColor(Color.TRANSPARENT);
        PieData pieData = new PieData(dataSet);
        chart.setUsePercentValues(true);
        chart.setData(pieData);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(70f);
        chart.setTransparentCircleRadius(75f);
        chart.setDrawEntryLabels(false);
        chart.setCenterText(String.format(Locale.getDefault(), "%.0f%%\nUsed", rawPercent));
        chart.setCenterTextSize(18f);
        chart.setCenterTextColor(centerTextColor);
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        if (isAdded()) {
            legend.setTextColor(getThemeTextColor());
        }
        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupCombinedBarChart() {
        if (usageViewModel == null || combinedBarChart == null) return;
        Map<String, Long> waterMap = usageViewModel.weeklyWaterData.getValue();
        Map<String, Long> elecMap = usageViewModel.weeklyElectricityData.getValue();
        if (waterMap == null || elecMap == null || waterMap.isEmpty()) {
            combinedBarChart.clear();
            combinedBarChart.invalidate();
            return;
        }
        ArrayList<String> labels = new ArrayList<>(waterMap.keySet());
        ArrayList<BarEntry> waterEntries = new ArrayList<>();
        ArrayList<BarEntry> elecEntries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            String date = labels.get(i);
            waterEntries.add(new BarEntry(i, waterMap.getOrDefault(date, 0L)));
            elecEntries.add(new BarEntry(i, elecMap.getOrDefault(date, 0L)));
        }
        int textColor = getThemeTextColor();
        BarDataSet waterSet = new BarDataSet(waterEntries, "Water (L)");
        waterSet.setColor(Color.parseColor("#00BCD4"));
        waterSet.setDrawValues(true);
        waterSet.setValueTextColor(textColor);
        BarDataSet elecSet = new BarDataSet(elecEntries, "Electricity (kWh)");
        elecSet.setColor(Color.parseColor("#2196F3"));
        elecSet.setDrawValues(true);
        elecSet.setValueTextColor(textColor);
        float barWidth = 0.4f;
        float barSpace = 0.03f;
        float groupSpace = 0.14f;
        BarData barData = new BarData(waterSet, elecSet);
        barData.setBarWidth(barWidth);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                return String.valueOf((int) value);
            }
        });
        barData.setValueTextSize(9f);
        combinedBarChart.setData(barData);
        combinedBarChart.getDescription().setEnabled(false);
        combinedBarChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = combinedBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(textColor);
        Legend legend = combinedBarChart.getLegend();
        legend.setTextColor(textColor);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        XAxis xAxis = combinedBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels.stream().map(this::formatDateToDay).collect(Collectors.toList())));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(labels.size());
        combinedBarChart.groupBars(0, groupSpace, barSpace);
        combinedBarChart.invalidate();
        // More dynamic animation for bar chart
        combinedBarChart.animateXY(1500, 1500);
    }

    private String formatDateToDay(String dateStr) {
        try {
            Date date = sdf.parse(dateStr);
            return dayFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    private int getThemeTextColor() {
        TypedValue typedValue = new TypedValue();
        Context context = getContext();
        if (context == null) return Color.BLACK;
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int color = arr.getColor(0, Color.BLACK);
        arr.recycle();
        return color;
    }

    private void showDatePicker() {
        if (!isAdded()) return;
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            cal.set(year, month, day);
            String newSelectedDate = sdf.format(cal.getTime());
            usageViewModel.setSelectedDate(newSelectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        if (!isAdded()) return;
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            etSelectTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    // Animation for percentage text
    private void animatePercentage(TextView textView, float start, float end) {
        if (getContext() == null) {
            textView.setText(String.format(Locale.getDefault(), "%.0f%% Used", end));
            return;
        }
        ValueAnimator animator = ValueAnimator.ofInt((int) start, (int) end);
        animator.setDuration(1000);
        animator.addUpdateListener(animation ->
                textView.setText(String.format(Locale.getDefault(), "%d%% Used", (int) animation.getAnimatedValue()))
        );
        animator.start();
    }
}