package com.example.myweather;

public class HourlyData {
    private String time;
    private int icon; // Sử dụng int để lưu ID của drawable
    private String temperature;

    // Constructor
    public HourlyData(String time, int icon, String temperature) {
        this.time = time;
        this.icon = icon;
        this.temperature = temperature;
    }

    // Getters
    public String getTime() {
        return time;
    }

    public int getIcon() {
        return icon;
    }

    public String getTemperature() {
        return temperature;
    }
}
