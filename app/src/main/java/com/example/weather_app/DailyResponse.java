package com.example.weather_app;

import com.google.gson.annotations.SerializedName;

public class DailyResponse {

    @SerializedName("date")
    private String date;

    @SerializedName("Nhiệt độ cao nhất")
    private float tempMax;

    @SerializedName("Nhiệt độ thấp nhất")
    private float tempMin;

    @SerializedName("Trạng thái thời tiết")
    private String weather;

    public float getTempMax() { return tempMax; }
    public String getDate() { return date; }
    public float getTempMin() { return tempMin; }
    public String getWeather() { return weather; }
}

