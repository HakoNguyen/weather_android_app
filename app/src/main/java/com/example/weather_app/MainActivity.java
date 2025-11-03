package com.example.weather_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.weather_app.databinding.ActivityMainBinding;
import com.example.weather_app.databinding.ItemWeatherDetailBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    private WeatherService weatherService;

    private List<HourlyResponse> hourlyWeatherList = new ArrayList<>();
    private List<DailyResponse> dailyWeatherList = new ArrayList<>();
    private HourlyWeatherAdapter hourlyWeatherAdapter;
    private DailyWeatherAdapter dailyWeatherAdapter;

    private String currentConditionText = null;
    private DailyResponse currentDayData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        weatherService = RetrofitClient.getApiService();

        setupRecyclerView();
        setupSearchView();
        fetchWeatherData("Hanoi");
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchWeatherData(query);
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupRecyclerView() {
        hourlyWeatherAdapter = new HourlyWeatherAdapter(hourlyWeatherList);
        binding.includeHourly.rvHourlyForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.includeHourly.rvHourlyForecast.setAdapter(hourlyWeatherAdapter);

        dailyWeatherAdapter = new DailyWeatherAdapter(dailyWeatherList);
        binding.includeDaily.rvDailyForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.includeDaily.rvDailyForecast.setAdapter(dailyWeatherAdapter);
    }

    private void fetchWeatherData(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            Toast.makeText(this, "Enter city name:", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Fetching data for: " + cityName);

        binding.searchView.setQuery(cityName, false);

        currentConditionText = null;
        currentDayData = null;
        binding.includeCurrent.tvCurrentTemp.setText("..°");
        binding.includeCurrent.tvCurrentCondition.setText("...");

        fetchHourlyWeather(cityName);
        fetchDailyWeather(cityName);
    }

    private void fetchHourlyWeather(String cityName) {
        weatherService.getHourlyWeather(cityName).enqueue(new Callback<List<HourlyResponse>>() {
            @Override
            public void onResponse(Call<List<HourlyResponse>> call, Response<List<HourlyResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu cho thành phố này",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<HourlyResponse> responseList = response.body();
                    HourlyResponse currentHour = responseList.get(0);

                    hourlyWeatherAdapter.updateData(responseList);
                    updateWeatherDetailsUI(currentHour);

                    binding.includeCurrent.tvCurrentTemp.setText(
                            String.format(Locale.getDefault(), "%.0f°", currentHour.getTemperature()));
                    currentConditionText = currentHour.getWeather();
                    updateMinMaxCondition();

                } else {
                    Log.e(TAG, "Error fetching hourly weather data: " + response.message());
                    Toast.makeText(MainActivity.this, "Lỗi máy chủ (hourly): " + response.message(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<HourlyResponse>> call, Throwable t) {
                Log.e(TAG, "Hourly API call failed", t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDailyWeather(String cityName) {
        weatherService.getDailyWeather(cityName).enqueue(new Callback<List<DailyResponse>>() {
            @Override
            public void onResponse(Call<List<DailyResponse>> call, Response<List<DailyResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().isEmpty()) {
                        Log.w(TAG, "Daily weather data is empty for " + cityName);
                        return;
                    }

                    List<DailyResponse> responseList = response.body();

                    currentDayData = responseList.get(0);
                    dailyWeatherAdapter.updateData(responseList);
                    updateMinMaxCondition();

                    binding.includeSunMoon.getRoot().setVisibility(View.GONE);
                } else {
                    Log.e(TAG, "Error fetching daily weather data: " + response.message());
                    Toast.makeText(MainActivity.this, "Lỗi máy chủ (daily): " + response.message(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<DailyResponse>> call, Throwable t) {
                Log.e(TAG, "Daily API call failed", t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMinMaxCondition() {
        if (currentConditionText != null && currentDayData != null) {
            binding.includeCurrent.tvCurrentCondition.setText(
                    String.format(Locale.getDefault(), "%s  %.0f° / %.0f°",
                            currentConditionText,
                            currentDayData.getTempMin(),
                            currentDayData.getTempMax()));
            currentConditionText = null;
            currentDayData = null;
        }
    }

    private void updateWeatherDetailsUI(HourlyResponse currentHour) {
        ItemWeatherDetailBinding uv = binding.includeDetails.detailUv;
        ItemWeatherDetailBinding feelsLike = binding.includeDetails.detailFeelsLike;
        ItemWeatherDetailBinding humidity = binding.includeDetails.detailHumidity;
        ItemWeatherDetailBinding wind = binding.includeDetails.detailWind;
        ItemWeatherDetailBinding pressure = binding.includeDetails.detailPressure;
        ItemWeatherDetailBinding visibility = binding.includeDetails.detailVisibility;

        uv.tvDetailTitle.setText("UV");
        uv.ivDetailIcon.setImageResource(R.drawable.ic_uv_index);
        uv.tvDetailValue.setText(String.format(Locale.getDefault(), "%.2f", currentHour.getUvIndex()));
        uv.tvDetailUnitOrDescription.setText(getUvDescription(currentHour.getUvIndex()));

        feelsLike.tvDetailTitle.setText("Lượng mưa");
        feelsLike.ivDetailIcon.setImageResource(R.drawable.ic_rainy);
        feelsLike.tvDetailValue.setText(String.format(Locale.getDefault(), "%.1f mm", currentHour.getRain()));
        feelsLike.tvDetailUnitOrDescription.setVisibility(View.GONE);

        humidity.tvDetailTitle.setText("Độ ẩm");
        humidity.ivDetailIcon.setImageResource(R.drawable.ic_humidity);
        humidity.tvDetailValue.setText(String.format(Locale.getDefault(), "%d%%", currentHour.getHumidity()));
        humidity.tvDetailUnitOrDescription.setVisibility(View.GONE);

        wind.tvDetailTitle.setText("Gió");
        wind.ivDetailIcon.setImageResource(R.drawable.ic_wind);
        wind.tvDetailValue.setText(String.format(Locale.getDefault(), "%.0f km/h", currentHour.getWindSpeed()));
        wind.tvDetailUnitOrDescription.setText(currentHour.getWindDirection());

        pressure.tvDetailTitle.setText("Áp suất");
        pressure.ivDetailIcon.setImageResource(R.drawable.ic_pressure);
        pressure.tvDetailValue.setText(String.format(Locale.getDefault(), "%.0f hPa", currentHour.getPressure()));
        pressure.tvDetailUnitOrDescription.setVisibility(View.GONE);

        visibility.tvDetailTitle.setText("Tầm nhìn");
        visibility.ivDetailIcon.setImageResource(R.drawable.ic_visibility);
        // API visibility is in meters; convert to kilometers for display
        double visibilityKm = currentHour.getVisibility() / 1000.0;
        visibility.tvDetailValue.setText(String.format(Locale.getDefault(), "%.0f km", visibilityKm));
        visibility.tvDetailUnitOrDescription.setVisibility(View.GONE);
    }

    private String getUvDescription(float uvIndex) {
        if (uvIndex <= 2f)
            return "Rất yếu";
        if (uvIndex <= 5f)
            return "Vừa";
        if (uvIndex <= 7f)
            return "Cao";
        if (uvIndex <= 10f)
            return "Rất cao";
        return "Cực cao";
    }
}
