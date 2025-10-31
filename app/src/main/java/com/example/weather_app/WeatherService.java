package com.example.weather_app;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather_hourly")
    Call<List<HourlyResponse>> getHourlyWeather(@Query("city") String cityName);

    @GET("next_days")
    Call<List<DailyResponse>> getDailyWeather(@Query("city") String cityName);

}
