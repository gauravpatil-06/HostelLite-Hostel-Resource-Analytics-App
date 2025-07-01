package com.example.trackingapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UsageViewModel extends AndroidViewModel {

    private static final String TAG = "UsageViewModel";
    private static final String PREFS_NAME = "ResourceTrackerPrefs";
    private static final String WATER_DATA_KEY = "waterDataMap";
    private static final String ELECTRICITY_DATA_KEY = "electricityDataMap";
    private static final String WATER_GOAL_KEY = "idealWaterGoal";
    private static final String ELECTRICITY_GOAL_KEY = "idealElectricityGoal";
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    private final FirebaseFirestore firestore;
    private final String userId = "user123";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public final MutableLiveData<Long> minWaterGoal = new MutableLiveData<>(0L);
    public final MutableLiveData<Long> idealWaterGoal = new MutableLiveData<>(0L);
    public final MutableLiveData<Long> maxWaterGoal = new MutableLiveData<>(0L);
    public final MutableLiveData<Long> minElectricityGoal = new MutableLiveData<>(0L);
    public final MutableLiveData<Long> idealElectricityGoal = new MutableLiveData<>(0L);
    public final MutableLiveData<Long> maxElectricityGoal = new MutableLiveData<>(0L);

    public final MutableLiveData<Map<String, Long>> allWaterData = new MutableLiveData<>();
    public final MutableLiveData<Map<String, Long>> allElectricityData = new MutableLiveData<>();

    public final MutableLiveData<Map<String, Long>> weeklyWaterData = new MutableLiveData<>();
    public final MutableLiveData<Map<String, Long>> weeklyElectricityData = new MutableLiveData<>();
    public final MutableLiveData<Map<String, Long>> monthlyAggregatedWaterData = new MutableLiveData<>();
    public final MutableLiveData<Map<String, Long>> monthlyAggregatedElectricityData = new MutableLiveData<>();

    // Shared selected date for all fragments
    public final MutableLiveData<String> selectedDate = new MutableLiveData<>();

    public UsageViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();

        // Set initial selected date to today
        selectedDate.setValue(sdf.format(new Date()));

        loadGoals();
        fetchData();
    }

    // Method to allow fragments to update the selected date
    public void setSelectedDate(String date) {
        selectedDate.setValue(date);
    }

    private Map<String, Long> getDataMap(String key) {
        String json = sharedPreferences.getString(key, "{}");
        Type type = new TypeToken<HashMap<String, Long>>() {}.getType();
        Map<String, Long> map = gson.fromJson(json, type);
        return map != null ? map : new HashMap<>();
    }

    private void saveDataMap(String key, Map<String, Long> map) {
        String json = gson.toJson(map);
        sharedPreferences.edit().putString(key, json).apply();
    }

    public void saveGoals(long idealWater, long idealElectricity) {
        long minWater = (long) (idealWater * 0.80);
        long maxWater = (long) (idealWater * 1.20);
        long minElec = (long) (idealElectricity * 0.80);
        long maxElec = (long) (idealElectricity * 1.20);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("minWaterGoal", minWater);
        editor.putLong(WATER_GOAL_KEY, idealWater);
        editor.putLong("maxWaterGoal", maxWater);
        editor.putLong("minElectricityGoal", minElec);
        editor.putLong(ELECTRICITY_GOAL_KEY, idealElectricity);
        editor.putLong("maxElectricityGoal", maxElec);
        editor.apply();

        idealWaterGoal.setValue(idealWater);
        minWaterGoal.setValue(minWater);
        maxWaterGoal.setValue(maxWater);
        idealElectricityGoal.setValue(idealElectricity);
        minElectricityGoal.setValue(minElec);
        maxElectricityGoal.setValue(maxElec);
    }

    private void loadGoals() {
        idealWaterGoal.setValue(sharedPreferences.getLong(WATER_GOAL_KEY, 0L));
        minWaterGoal.setValue(sharedPreferences.getLong("minWaterGoal", 0L));
        maxWaterGoal.setValue(sharedPreferences.getLong("maxWaterGoal", 0L));
        idealElectricityGoal.setValue(sharedPreferences.getLong(ELECTRICITY_GOAL_KEY, 0L));
        minElectricityGoal.setValue(sharedPreferences.getLong("minElectricityGoal", 0L));
        maxElectricityGoal.setValue(sharedPreferences.getLong("maxElectricityGoal", 0L));
    }

    public void saveUsageData(Map<String, Object> usageDataMap) {
        executor.execute(() -> {
            String date = (String) usageDataMap.get("date");
            if (date == null) return;

            long waterUsed = (long) usageDataMap.get("waterUsed");
            long electricityUsed = (long) usageDataMap.get("electricityUsed");

            Map<String, Long> waterData = getDataMap(WATER_DATA_KEY);
            waterData.put(date, waterUsed);
            saveDataMap(WATER_DATA_KEY, waterData);

            Map<String, Long> elecData = getDataMap(ELECTRICITY_DATA_KEY);
            elecData.put(date, electricityUsed);
            saveDataMap(ELECTRICITY_DATA_KEY, elecData);

            DocumentReference dailyDocRef = firestore.collection("UsageData").document(userId).collection("daily").document(date);
            dailyDocRef.set(usageDataMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Firestore daily total updated successfully."))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating Firestore daily total.", e));

            getApplication().getMainExecutor().execute(this::fetchData);
        });
    }

    public void fetchData() {
        executor.execute(() -> {
            Map<String, Long> waterData = getDataMap(WATER_DATA_KEY);
            Map<String, Long> elecData = getDataMap(ELECTRICITY_DATA_KEY);

            allWaterData.postValue(waterData);
            allElectricityData.postValue(elecData);

            LinkedHashMap<String, Long> weeklyWaterMap = new LinkedHashMap<>();
            LinkedHashMap<String, Long> weeklyElecMap = new LinkedHashMap<>();
            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < 7; i++) {
                String dateStr = sdf.format(cal.getTime());
                weeklyWaterMap.put(dateStr, waterData.getOrDefault(dateStr, 0L));
                weeklyElecMap.put(dateStr, elecData.getOrDefault(dateStr, 0L));
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }

            List<String> keys = new ArrayList<>(weeklyWaterMap.keySet());
            java.util.Collections.reverse(keys);

            LinkedHashMap<String, Long> finalWeeklyWater = new LinkedHashMap<>();
            LinkedHashMap<String, Long> finalWeeklyElec = new LinkedHashMap<>();
            for (String key : keys) {
                finalWeeklyWater.put(key, weeklyWaterMap.get(key));
                finalWeeklyElec.put(key, weeklyElecMap.get(key));
            }
            weeklyWaterData.postValue(finalWeeklyWater);
            weeklyElectricityData.postValue(finalWeeklyElec);

            Map<String, Long> monthlyWaterAggregates = waterData.entrySet().stream()
                    .collect(Collectors.groupingBy(entry -> entry.getKey().substring(0, 7),
                            LinkedHashMap::new, Collectors.summingLong(Map.Entry::getValue)));
            monthlyAggregatedWaterData.postValue(monthlyWaterAggregates);

            Map<String, Long> monthlyElecAggregates = elecData.entrySet().stream()
                    .collect(Collectors.groupingBy(entry -> entry.getKey().substring(0, 7),
                            LinkedHashMap::new, Collectors.summingLong(Map.Entry::getValue)));
            monthlyAggregatedElectricityData.postValue(monthlyElecAggregates);
        });
    }
}