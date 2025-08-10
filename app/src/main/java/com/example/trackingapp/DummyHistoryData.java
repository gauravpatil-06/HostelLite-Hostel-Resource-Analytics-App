package com.example.trackingapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyHistoryData {

    // ⭐ DUMMY DATA SETS (For 10 users, each with 3 dummy entries)
    private static final List<List<Map<String, Object>>> HISTORY_DATA = new ArrayList<>();

    static {
        // Data for User 1 (Example User ID: "user123")
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "10:00", 500L, 2.5, 23.5, 2L),
                createEntry("2025-10-03", "14:30", 550L, 2.8, 24.1, 3L),
                createEntry("2025-10-02", "09:15", 480L, 2.3, 22.9, 1L)
        ));
        // Data for User 2 (Example User ID: "user124")
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "12:00", 750L, 4.0, 25.0, 4L),
                createEntry("2025-10-03", "18:00", 600L, 3.5, 26.2, 2L),
                createEntry("2025-10-02", "11:45", 700L, 3.8, 24.8, 3L)
        ));
        // Data for User 3 (Example User ID: "user125")
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "08:30", 900L, 5.0, 22.0, 1L),
                createEntry("2025-10-03", "17:10", 950L, 5.2, 23.0, 2L),
                createEntry("2025-10-02", "10:00", 850L, 4.8, 21.5, 1L)
        ));
        // Data for User 4
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "11:00", 300L, 1.5, 20.0, 1L),
                createEntry("2025-10-03", "13:00", 320L, 1.6, 20.5, 1L),
                createEntry("2025-10-02", "15:00", 310L, 1.5, 20.2, 1L)
        ));
        // Data for User 5
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "16:00", 1200L, 6.0, 28.0, 5L),
                createEntry("2025-10-03", "19:00", 1150L, 5.8, 27.5, 4L),
                createEntry("2025-10-02", "20:00", 1250L, 6.2, 28.5, 6L)
        ));
        // Data for User 6
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "07:00", 400L, 2.0, 21.0, 1L),
                createEntry("2025-10-03", "07:30", 420L, 2.1, 21.3, 1L),
                createEntry("2025-10-02", "08:00", 410L, 2.0, 21.1, 1L)
        ));
        // Data for User 7
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "15:00", 650L, 3.2, 25.5, 2L),
                createEntry("2025-10-03", "16:30", 680L, 3.4, 25.8, 3L),
                createEntry("2025-10-02", "18:45", 640L, 3.1, 25.3, 2L)
        ));
        // Data for User 8
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "14:00", 1000L, 5.5, 27.0, 3L),
                createEntry("2025-10-03", "17:30", 1050L, 5.7, 27.3, 4L),
                createEntry("2025-10-02", "19:15", 980L, 5.3, 26.8, 3L)
        ));
        // Data for User 9
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "13:30", 250L, 1.2, 19.0, 1L),
                createEntry("2025-10-03", "14:00", 270L, 1.3, 19.3, 1L),
                createEntry("2025-10-02", "15:30", 260L, 1.2, 19.1, 1L)
        ));
        // Data for User 10
        HISTORY_DATA.add(Arrays.asList(
                createEntry("2025-10-04", "21:00", 800L, 4.5, 24.5, 2L),
                createEntry("2025-10-03", "22:00", 820L, 4.6, 24.7, 3L),
                createEntry("2025-10-02", "23:00", 780L, 4.4, 24.3, 2L)
        ));
    }

    private static Map<String, Object> createEntry(String date, String time, Long waterUsed, Double electricityUsed, Double temperature, Long occupancy) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("time", time);
        map.put("waterUsed", waterUsed);
        map.put("electricityUsed", electricityUsed); // Use Double for kWh
        map.put("temperature", temperature);
        map.put("occupancy", occupancy);
        return map;
    }

    /**
     * Retrieves dummy usage history data based on a user's ID.
     * The logic cycles through the 10 data sets (1-10, then back to 1).
     * @param userId The unique user ID (e.g., "user123").
     * @return List of dummy usage data maps.
     */
    public static List<Map<String, Object>> getHistoryForUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Collections.emptyList();
        }

        // Simple hashing function to map any UID to an index between 0 and 9
        int hashCode = userId.hashCode();
        int dataIndex = Math.abs(hashCode) % HISTORY_DATA.size();

        return HISTORY_DATA.get(dataIndex);
    }
}