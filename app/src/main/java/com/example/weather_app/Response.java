package com.example.weather_app;

import com.squareup.moshi.Json;

public class Response {
    public String city;
    public String country;
    @Json(name = "temperature")
    public double temp;
    public double temp_min;
    public double temp_max;
    public int pressure;
    public int humidity;
    public double wind_speed;
    @Json(name = "weather_main")
    public String weatherMain;
    @Json(name = "weather_description")
    public String weatherDescription;
}
