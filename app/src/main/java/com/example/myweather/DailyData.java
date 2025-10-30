package com.example.myweather;

public class DailyData {
    private String day;
    private int icon;
    private String tempRange;

    public DailyData(String day, int icon, String tempRange) {
        this.day = day;
        this.icon = icon;
        this.tempRange = tempRange;
    }

    public String getDay() {
        return day;
    }

    public int getIcon() {
        return icon;
    }

    public String getTempRange() {
        return tempRange;
    }
}
