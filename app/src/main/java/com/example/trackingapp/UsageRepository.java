package com.example.trackingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class UsageRepository {

    private static final String TAG = "UsageRepository";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String userId = "hostel_admin_01";
    private final CollectionReference dailyUsageRef = firestore.collection("UsageData").document(userId).collection("daily");

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final String PREFS_NAME = "HostelUsagePrefs";
    private final SharedPreferences prefs;

    public UsageRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- GOAL MANAGEMENT ---
    public void saveGoals(long waterGoal, long electricityGoal) {
        prefs.edit()
                .putLong("waterGoal", waterGoal)
                .putLong("electricityGoal", electricityGoal)
                .apply();
    }

    public long getWaterGoal() {
        return prefs.getLong("waterGoal", 1000L); // Default 1000L
    }

    public long getElectricityGoal() {
        return prefs.getLong("electricityGoal", 500L); // Default 500kWh
    }

    // --- DATA SUBMISSION ---
    public void saveUsageData(Map<String, Object> usageData, final OnDataSaveListener listener) {
        dailyUsageRef.document()
                .set(usageData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    // --- DATA FETCHING ---
    public void listenForTodaysUsage(MutableLiveData<Long> todayWater, MutableLiveData<Long> todayElectricity) {
        String todayDate = sdf.format(new Date());
        dailyUsageRef.whereEqualTo("date", todayDate)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen for today's usage failed.", e);
                        return;
                    }

                    long totalWater = 0;
                    long totalElectricity = 0;
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            if (doc.getLong("waterUsed") != null) {
                                totalWater += doc.getLong("waterUsed");
                            }
                            if (doc.getLong("electricityUsed") != null) {
                                totalElectricity += doc.getLong("electricityUsed");
                            }
                        }
                    }
                    todayWater.postValue(totalWater);
                    todayElectricity.postValue(totalElectricity);
                });
    }

    public void fetchHistoricalUsage(MutableLiveData<Map<String, Long>> weeklyWater, MutableLiveData<Map<String, Long>> weeklyElec,
                                     MutableLiveData<Map<String, Long>> monthlyWater, MutableLiveData<Map<String, Long>> monthlyElec) {

        // Fetch data for the whole year to populate both weekly and monthly charts in one go
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -1);
        String startDate = sdf.format(cal.getTime());

        dailyUsageRef.whereGreaterThanOrEqualTo("date", startDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Long> waterTotalPerDay = new TreeMap<>();
                    Map<String, Long> electricityTotalPerDay = new TreeMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String dateStr = document.getString("date");
                        Long water = document.getLong("waterUsed");
                        Long electricity = document.getLong("electricityUsed");

                        if (dateStr != null && water != null && electricity != null) {
                            waterTotalPerDay.put(dateStr, waterTotalPerDay.getOrDefault(dateStr, 0L) + water);
                            electricityTotalPerDay.put(dateStr, electricityTotalPerDay.getOrDefault(dateStr, 0L) + electricity);
                        }
                    }
                    // Post the full map for monthly aggregation
                    monthlyWater.postValue(waterTotalPerDay);
                    monthlyElec.postValue(electricityTotalPerDay);

                    // Filter the map for the last 7 days for weekly charts
                    weeklyWater.postValue(filterLast7Days(waterTotalPerDay));
                    weeklyElec.postValue(filterLast7Days(electricityTotalPerDay));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch historical data.", e);
                });
    }

    private Map<String, Long> filterLast7Days(Map<String, Long> fullData) {
        Map<String, Long> filteredMap = new TreeMap<>();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6);
        for(int i = 0; i < 7; i++) {
            String dateKey = sdf.format(cal.getTime());
            filteredMap.put(dateKey, fullData.getOrDefault(dateKey, 0L));
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        return filteredMap;
    }

    public interface OnDataSaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}