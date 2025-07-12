package com.example.trackingapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class temperatureActivity extends AppCompatActivity {

    private static final String[] CITIES = new String[]{
            "Agra", "Ahmedabad", "Akola", "Aligarh", "Allahabad", "Amravati", "Amritsar", "Asansol", "Aurangabad",
            "Bareilly", "Beed", "Belagavi", "Bengaluru", "Bhatpara", "Bhavnagar", "Bhilai", "Bhiwandi", "Bhopal",
            "Bhubaneswar", "Bikaner", "Chandigarh", "Chennai", "Coimbatore", "Cuttack", "Dehradun", "Delhi",
            "Dhanbad", "Dhule", "Durgapur", "Erode", "Faridabad", "Firozabad", "Gaya", "Ghaziabad", "Gorakhpur",
            "Gulbarga", "Guntur", "Gurgaon", "Guwahati", "Gwalior", "Howrah", "Hubballi-Dharwad", "Hyderabad",
            "Indore", "Jabalpur", "Jaipur", "Jalandhar", "Jalgaon", "Jammu", "Jamnagar", "Jamshedpur", "Jhansi",
            "Jodhpur", "Kalyan-Dombivali", "Kanpur", "Kochi", "Kolhapur", "Kolkata", "Kota", "Kurnool", "Latur",
            "Loni", "Lucknow", "Ludhiana", "Madurai", "Maheshtala", "Malegaon", "Mangaluru", "Meerut", "Moradabad",
            "Mumbai", "Mysuru", "Nagpur", "Nanded", "Nashik", "Navi Mumbai", "Nellore", "Noida", "Panvel", "Patna",
            "Pimpri-Chinchwad", "Pune", "Raipur", "Rajkot", "Ranchi", "Ratnagiri", "Rourkela", "Saharanpur",
            "Salem", "Sangli", "Sangli-Miraj & Kupwad", "Satara", "Siliguri", "Solapur", "Srinagar", "Surat",
            "Thane", "Tiruchirappalli", "Tirunelveli", "Tiruppur", "Udaipur", "Ujjain", "Ulhasnagar", "Vadodara",
            "Varanasi", "Vasai-Virar", "Vijayawada", "Visakhapatnam", "Warangal", "Wardha", "Yavatmal"
    };

    private AutoCompleteTextView actvCityName;
    private TextInputEditText etSelectDate, etTemperature;
    private Button btnSaveTemperature;
    private TextView tvLatestCity, tvLatestTemperature, tvPercentageChange;
    private BarChart barChartWeekly, barChartMonthly, barChartYearly;

    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        setTitle("Temperature Analysis");

        sharedPreferences = getSharedPreferences("HostelLitePrefs", Context.MODE_PRIVATE);
        initializeUI();
        setupEventListeners();
        loadDataForCity(getLastSelectedCity());
    }

    private void initializeUI() {
        actvCityName = findViewById(R.id.actvCityName);
        etSelectDate = findViewById(R.id.etSelectDate);
        etTemperature = findViewById(R.id.etTemperature);
        btnSaveTemperature = findViewById(R.id.btnSaveTemperature);
        tvLatestCity = findViewById(R.id.tvLatestCity);
        tvLatestTemperature = findViewById(R.id.tvLatestTemperature);
        tvPercentageChange = findViewById(R.id.tvPercentageChange);
        barChartWeekly = findViewById(R.id.barChartWeekly);
        barChartMonthly = findViewById(R.id.barChartMonthly);
        barChartYearly = findViewById(R.id.barChartYearly);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CITIES);
        actvCityName.setAdapter(cityAdapter);

        etSelectDate.setText(sdf.format(new Date()));
    }

    private void setupEventListeners() {
        btnSaveTemperature.setOnClickListener(v -> saveTemperatureData());
        actvCityName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            loadDataForCity(selectedCity);
        });
    }

    private void saveTemperatureData() {
        String city = actvCityName.getText().toString().trim();
        String date = etSelectDate.getText().toString().trim();
        String tempStr = etTemperature.getText().toString().trim();

        if (city.isEmpty() || date.isEmpty() || tempStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double temp = Double.parseDouble(tempStr);
            String dataKey = "temperature_data_" + city.toLowerCase();
            Map<String, Double> temperatureData = getTemperatureDataForCity(city);
            temperatureData.put(date, temp);

            String jsonString = gson.toJson(temperatureData);
            sharedPreferences.edit().putString(dataKey, jsonString).apply();
            sharedPreferences.edit().putString("last_selected_city", city).apply();

            Toast.makeText(this, "Temperature for " + city + " saved!", Toast.LENGTH_SHORT).show();
            etTemperature.setText("");
            loadDataForCity(city);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid temperature", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataForCity(String city) {
        if (city == null || city.isEmpty()) {
            city = "Pune"; // Default city
        }

        actvCityName.setText(city, false);
        Map<String, Double> data = getTemperatureDataForCity(city);

        tvLatestCity.setText(city);

        if (data.isEmpty()) {
            tvLatestTemperature.setText("--°C");
            tvPercentageChange.setText("No data to compare");
            tvPercentageChange.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            List<String> sortedDates = new ArrayList<>(data.keySet());
            Collections.sort(sortedDates, Collections.reverseOrder());

            Double latestTemp = data.get(sortedDates.get(0));
            tvLatestTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", latestTemp));

            if (sortedDates.size() > 1) {
                Double previousTemp = data.get(sortedDates.get(1));
                if (previousTemp != 0) { // Avoid division by zero
                    double change = ((latestTemp - previousTemp) / previousTemp) * 100;
                    if (change > 0) {
                        tvPercentageChange.setText(String.format(Locale.getDefault(), "%.1f%% warmer vs yesterday", change));
                        tvPercentageChange.setTextColor(Color.parseColor("#D32F2F")); // Red
                        tvPercentageChange.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_up, 0, 0, 0);
                    } else {
                        tvPercentageChange.setText(String.format(Locale.getDefault(), "%.1f%% cooler vs yesterday", Math.abs(change)));
                        tvPercentageChange.setTextColor(Color.parseColor("#388E3C")); // Green
                        tvPercentageChange.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_down, 0, 0, 0);
                    }
                }
            } else {
                tvPercentageChange.setText("Needs one more entry to compare");
                tvPercentageChange.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
        setupWeeklyChart(data);
        setupMonthlyChart(data);
        setupYearlyChart(data);
    }

    private Map<String, Double> getTemperatureDataForCity(String city) { String dataKey = "temperature_data_" + city.toLowerCase(); String json = sharedPreferences.getString(dataKey, "{}"); Type type = new TypeToken<LinkedHashMap<String, Double>>(){}.getType(); return gson.fromJson(json, type); }
    private String getLastSelectedCity() { return sharedPreferences.getString("last_selected_city", "Pune"); }
    private void showDatePicker() { Calendar cal = Calendar.getInstance(); new DatePickerDialog(this, (view, year, month, day) -> { cal.set(year, month, day); etSelectDate.setText(sdf.format(cal.getTime())); }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show(); }

    private void setupWeeklyChart(Map<String, Double> data) {
        if (data.isEmpty()) { barChartWeekly.clear(); barChartWeekly.invalidate(); return; }
        List<String> sortedDates = new ArrayList<>(data.keySet()); Collections.sort(sortedDates, Collections.reverseOrder());
        List<String> labels = new ArrayList<>(); ArrayList<BarEntry> entries = new ArrayList<>();
        SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        int limit = Math.min(7, sortedDates.size());
        for (int i = 0; i < limit; i++) {
            String dateStr = sortedDates.get(i);
            try { Date date = sdf.parse(dateStr); labels.add(dayMonthFormat.format(date)); } catch (ParseException e) { labels.add(dateStr.substring(5)); }
            entries.add(new BarEntry(i, data.get(dateStr).floatValue()));
        }
        Collections.reverse(labels); Collections.reverse(entries);
        BarDataSet dataSet = new BarDataSet(entries, "Temperature");
        dataSet.setColor(Color.parseColor("#F57C00"));
        BarData barData = new BarData(dataSet);

        // BADAL KELA: Bar width ajun kami keli
        barData.setBarWidth(0.2f);

        barChartWeekly.setData(barData);
        styleBarChart(barChartWeekly, labels);
        barChartWeekly.invalidate();
    }

    private void setupMonthlyChart(Map<String, Double> data) {
        if (data.isEmpty()) { barChartMonthly.clear(); barChartMonthly.invalidate(); return; }
        Map<String, List<Double>> monthlyAggregation = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        for(Map.Entry<String, Double> entry : data.entrySet()){
            try {
                Date date = sdf.parse(entry.getKey());
                String monthKey = monthFormat.format(date);
                monthlyAggregation.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(entry.getValue());
            } catch (ParseException e) { e.printStackTrace(); }
        }
        List<String> labels = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();
        int i = 0;
        SimpleDateFormat monthYearDisplayFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        for(Map.Entry<String, List<Double>> entry : monthlyAggregation.entrySet()){
            double average = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);
            entries.add(new BarEntry(i, (float) average));
            try { Date monthDate = monthFormat.parse(entry.getKey()); labels.add(monthYearDisplayFormat.format(monthDate)); } catch (ParseException e) { labels.add(entry.getKey()); }
            i++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Avg Temp");
        dataSet.setColor(Color.parseColor("#FB8C00"));
        BarData barData = new BarData(dataSet);

        // BADAL KELA: Bar width ajun kami keli
        barData.setBarWidth(0.2f);

        barChartMonthly.setData(barData);
        styleBarChart(barChartMonthly, labels);
        barChartMonthly.invalidate();
    }

    private void setupYearlyChart(Map<String, Double> data) {
        if (data.isEmpty()) { barChartYearly.clear(); barChartYearly.invalidate(); return; }
        Map<String, List<Double>> yearlyAggregation = new LinkedHashMap<>();
        for(Map.Entry<String, Double> entry : data.entrySet()){
            try {
                String yearKey = entry.getKey().substring(0, 4);
                yearlyAggregation.computeIfAbsent(yearKey, k -> new ArrayList<>()).add(entry.getValue());
            } catch (Exception e) { e.printStackTrace(); }
        }
        List<String> labels = new ArrayList<>(yearlyAggregation.keySet());
        Collections.sort(labels);
        ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i = 0; i < labels.size(); i++){
            String year = labels.get(i);
            double average = yearlyAggregation.get(year).stream().mapToDouble(val -> val).average().orElse(0.0);
            entries.add(new BarEntry(i, (float) average));
        }
        BarDataSet dataSet = new BarDataSet(entries, "Avg Temp");
        dataSet.setColor(Color.parseColor("#FFB74D"));
        BarData barData = new BarData(dataSet);

        // BADAL KELA: Bar width ajun kami keli
        barData.setBarWidth(0.2f);

        barChartYearly.setData(barData);
        styleBarChart(barChartYearly, labels);
        barChartYearly.invalidate();
    }

    private void styleBarChart(BarChart chart, List<String> labels) {
        int textColor = getThemeTextColor();
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setFitBars(true);
        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawGridLines(false);
        chart.animateY(1200, Easing.EaseInOutCubic);
    }

    private int getThemeTextColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int color = arr.getColor(0, Color.BLACK);
        arr.recycle();
        return color;
    }
}