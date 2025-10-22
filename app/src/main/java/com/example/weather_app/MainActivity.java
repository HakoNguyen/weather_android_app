package com.example.weather_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private TextView tvCity, tvTemp, tvDesc;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvDesc);

        fetchWeather("Hanoi");
    }

    private void fetchWeather(String cityName) {
        WeatherService weatherService = RetrofitClient.getApiService();
        Call<com.example.weather_app.Response> call = weatherService.getWeather(cityName);

        call.enqueue(new Callback<com.example.weather_app.Response>() {
            @Override
            public void onResponse(Call<com.example.weather_app.Response> call, retrofit2.Response<com.example.weather_app.Response> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.weather_app.Response weatherResponse = response.body();
                    tvCity.setText(String.format("%s, %s", weatherResponse.city, weatherResponse.country));
                    tvTemp.setText(String.format("%s°C", weatherResponse.temp));
                    tvDesc.setText(weatherResponse.weatherDescription);
                } else {
                    Log.e(TAG, "Response was not successful. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.weather_app.Response> call, Throwable t) {
                Log.e(TAG, "API call failed.", t);
            }
        });
    }
}
