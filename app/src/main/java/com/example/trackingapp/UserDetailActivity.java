package com.example.trackingapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    private TextView tvUserWaterGoal, tvUserElecGoal;
    private RecyclerView rvUsageHistory;
    private FirebaseFirestore firestore; // FirebaseFirestore is kept but not used for history fetch
    private UsageHistoryAdapter adapter;

    // ⭐ MODIFIED: Change the list type to List<Map<String, Object>>
    private List<Map<String, Object>> usageHistoryList = new ArrayList<>();
    private String userId, userEmail;

    private final long DEFAULT_WATER_GOAL = 1000L;
    private final long DEFAULT_ELEC_GOAL = 50L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        userId = getIntent().getStringExtra("USER_ID");
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        if (userId == null || userEmail == null) {
            Toast.makeText(this, "Error: User ID or Email missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbar_user_detail);
        toolbar.setTitle(userEmail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // View Initialization
        tvUserWaterGoal = findViewById(R.id.tvUserWaterGoal);
        tvUserElecGoal = findViewById(R.id.tvUserElecGoal);
        rvUsageHistory = findViewById(R.id.rvUsageHistory);
        firestore = FirebaseFirestore.getInstance();

        // Display default goals directly
        tvUserWaterGoal.setText(String.format(Locale.getDefault(), "💧 Water Goal: %d L", DEFAULT_WATER_GOAL));
        tvUserElecGoal.setText(String.format(Locale.getDefault(), "⚡️ Elec. Goal: %d kWh", DEFAULT_ELEC_GOAL));

        setupRecyclerView();

        // ⭐ CALL NEW DUMMY DATA LOADER
        loadDummyUsageHistory();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        try {
            // ⭐ MODIFIED: Adapter now accepts the new List<Map<String, Object>> type
            adapter = new UsageHistoryAdapter(usageHistoryList, this::showUsageDetailsDialog);
            rvUsageHistory.setAdapter(adapter);
            rvUsageHistory.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            Toast.makeText(this, "Error: UsageHistoryAdapter setup failed.", Toast.LENGTH_LONG).show();
        }
    }

    // ⭐ NEW FUNCTION: Loads data from the static DummyHistoryData class
    private void loadDummyUsageHistory() {
        // userId चा वापर करून डमी डेटा मिळवा
        List<Map<String, Object>> dummyData = DummyHistoryData.getHistoryForUser(userId);

        // जुना डेटा काढून टाका आणि नवीन डमी डेटा भरा
        usageHistoryList.clear();
        usageHistoryList.addAll(dummyData);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (usageHistoryList.isEmpty()) {
            Toast.makeText(this, "No usage history found for this user ID.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Dummy history loaded. Total items: " + usageHistoryList.size(), Toast.LENGTH_SHORT).show();
        }
    }

    // ⭐ MODIFIED: This function must now accept Map<String, Object>
    private void showUsageDetailsDialog(Map<String, Object> doc) {

        // Safely extract data from the Map
        String date = (String) doc.get("date");
        String time = (String) doc.get("time");
        Number water = (Number) doc.get("waterUsed");
        Number elec = (Number) doc.get("electricityUsed");
        Number temp = (Number) doc.get("temperature");
        Number occupancy = (Number) doc.get("occupancy");

        StringBuilder details = new StringBuilder();
        details.append("Date: ").append(date != null ? date : "N/A").append("\n");
        details.append("Time: ").append(time != null ? time : "N/A").append("\n\n");
        details.append("💧 Water Used: ").append(water != null ? water.longValue() : 0L).append(" L\n");
        details.append("⚡️ Electricity Used: ").append(elec != null ? String.format(Locale.getDefault(), "%.2f", elec.doubleValue()) : 0.0).append(" kWh\n\n");
        details.append("🌡️ Temperature: ").append(temp != null ? String.format(Locale.getDefault(), "%.1f", temp.doubleValue()) : "N/A").append(" °C\n");
        details.append("👥 Occupancy: ").append(occupancy != null ? occupancy.longValue() : "N/A");

        new AlertDialog.Builder(this)
                .setTitle("Usage Details for " + (date != null ? date : "N/A"))
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    // Original fetchUsageHistory() is removed.
}