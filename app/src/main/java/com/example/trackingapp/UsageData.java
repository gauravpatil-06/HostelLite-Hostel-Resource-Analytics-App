package com.example.trackingapp;

public class UsageData {
    private String date;
    private String time;
    private long waterUsed;
    private long electricityUsed;
    private long occupancy;
    private double temperature;

    public UsageData() {}

    public UsageData(String date, String time, long waterUsed, long electricityUsed, long occupancy, double temperature) {
        this.date = date;
        this.time = time;
        this.waterUsed = waterUsed;
        this.electricityUsed = electricityUsed;
        this.occupancy = occupancy;
        this.temperature = temperature;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public long getWaterUsed() { return waterUsed; }
    public long getElectricityUsed() { return electricityUsed; }
    public long getOccupancy() { return occupancy; }
    public double getTemperature() { return temperature; }
}