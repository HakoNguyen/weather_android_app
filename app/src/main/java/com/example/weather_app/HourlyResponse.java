package com.example.weather_app;

import com.google.gson.annotations.SerializedName;

public class HourlyResponse {

    @SerializedName("time")
    private String time;

    @SerializedName("temperature")
    private float temperature;

    @SerializedName("weather")
    private String weather;

    @SerializedName("cloud_cover")
    private int cloudCover;

    @SerializedName("uv_index")
    private int uvIndex;

    @SerializedName("humidity")
    private int humidity;

    @SerializedName("wind_speed")
    private float windSpeed;

    @SerializedName("wind_direction")
    private String windDirection;

    @SerializedName("wind_degree")
    private int windDegree;

    @SerializedName("pressure")
    private float pressure;

    @SerializedName("visibility")
    private int visibility;

    @SerializedName("rain")
    private float rain;

    public String getTime() { return time; }
    public float getTemperature() { return temperature; }
    public String getWeather() { return weather; }
    public int getCloudCover() { return cloudCover; }
    public int getUvIndex() { return uvIndex; }
    public int getHumidity() { return humidity; }
    public float getWindSpeed() { return windSpeed; }
    public String getWindDirection() { return windDirection; }
    public int getWindDegree() { return windDegree; }
    public float getPressure() { return pressure; }
    public int getVisibility() { return visibility; }
    public float getRain() { return rain; }
}

