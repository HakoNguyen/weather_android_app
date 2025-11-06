package com.example.weather_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.weather_app.databinding.ActivityMainBinding;
import com.example.weather_app.databinding.ItemWeatherDetailBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private ActivityMainBinding binding;

    private WeatherService weatherService;
    private PreferencesHelper preferencesHelper;
    private LocationHelper locationHelper;

    private List<HourlyResponse> hourlyWeatherList = new ArrayList<>();
    private List<DailyResponse> dailyWeatherList = new ArrayList<>();
    private HourlyWeatherAdapter hourlyWeatherAdapter;
    private DailyWeatherAdapter dailyWeatherAdapter;

    private String currentConditionText = null;
    private DailyResponse currentDayData = null;
    private String currentCity = "";
    private int pendingApiCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        weatherService = RetrofitClient.getApiService();
        preferencesHelper = new PreferencesHelper(this);
        locationHelper = new LocationHelper(this);

        setupRecyclerView();
        setupSearchView();
        setupLocationButton();
        setupMapButton();
        setupShareButton();
        setupSwipeRefresh();
        setupSettingsButton();
        
        // Tạo notification channel cho cảnh báo
        WeatherAlertHelper.createNotificationChannel(this);
        
        // Load last city or use GPS if enabled
        if (preferencesHelper.shouldUseGps() && LocationHelper.hasLocationPermission(this)) {
            getLocationAndFetchWeather();
        } else {
            String lastCity = preferencesHelper.getLastCity();
            fetchWeatherData(lastCity);
        }
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    fetchWeatherData(query.trim());
                    preferencesHelper.saveLastCity(query.trim());
                    binding.searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupLocationButton() {
        binding.ivLocation.setOnClickListener(v -> {
            if (LocationHelper.hasLocationPermission(this)) {
                getLocationAndFetchWeather();
            } else {
                requestLocationPermission();
            }
        });
    }

    private void setupMapButton() {
        binding.ivMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("current_city", currentCity); // Truyền thành phố hiện tại
            startActivityForResult(intent, 1002);
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!currentCity.isEmpty()) {
                fetchWeatherData(currentCity);
            } else {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupShareButton() {
        binding.ivShare.setOnClickListener(v -> shareWeather());
    }

    private void shareWeather() {
        if (currentCity == null || currentCity.isEmpty() || currentConditionText == null) {
            Toast.makeText(this, "Chưa có dữ liệu thời tiết để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy nhiệt độ hiện tại
        String tempStr = binding.includeCurrent.tvCurrentTemp.getText().toString();
        tempStr = tempStr.replace("°", "").trim();
        
        // Tạo nội dung chia sẻ
        StringBuilder shareText = new StringBuilder();
        shareText.append("🌤️ Thời tiết tại ").append(currentCity).append("\n\n");
        shareText.append("Nhiệt độ: ").append(tempStr).append("\n");
        shareText.append("Điều kiện: ").append(currentConditionText).append("\n");
        
        if (currentDayData != null) {
            boolean isFahrenheit = preferencesHelper.isFahrenheit();
            String minTemp = TemperatureUtils.formatTemperature(currentDayData.getTempMin(), isFahrenheit);
            String maxTemp = TemperatureUtils.formatTemperature(currentDayData.getTempMax(), isFahrenheit);
            shareText.append("Nhiệt độ: ").append(minTemp).append(" / ").append(maxTemp).append("\n");
        }
        
        shareText.append("\n📱 Từ ứng dụng Weather App");
        
        // Intent để chia sẻ
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Thời tiết tại " + currentCity);
        
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ thời tiết qua"));
    }

    private void setupSettingsButton() {
        binding.ivSettings.setOnClickListener(v -> showSettingsDialog());
    }

    private void getLocationAndFetchWeather() {
        showLoading(true);
        locationHelper.getCurrentCity(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationSuccess(String cityName) {
                Log.d(TAG, "Location found: " + cityName);
                currentCity = cityName;
                fetchWeatherData(cityName);
                preferencesHelper.saveLastCity(cityName);
            }

            @Override
            public void onLocationFailure(String error) {
                Log.e(TAG, "Location error: " + error);
                showLoading(false);
                Toast.makeText(MainActivity.this, "Không thể lấy vị trí: " + error, Toast.LENGTH_SHORT).show();
                // Fallback to last city
                String lastCity = preferencesHelper.getLastCity();
                fetchWeatherData(lastCity);
            }
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Cần quyền vị trí")
                    .setMessage("Ứng dụng cần quyền truy cập vị trí để tìm thời tiết tại vị trí của bạn.")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSettingsDialog() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("current_city", currentCity);
        startActivityForResult(intent, 1003); // Request code 1003 cho SettingsActivity
    }

    private void openFavoriteCitiesActivity() {
        Intent intent = new Intent(this, FavoriteCitiesActivity.class);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Kết quả từ FavoriteCitiesActivity (mở trực tiếp từ MainActivity)
            String selectedCity = data.getStringExtra("selected_city");
            if (selectedCity != null && !selectedCity.isEmpty()) {
                fetchWeatherData(selectedCity);
                preferencesHelper.saveLastCity(selectedCity);
            }
        } else if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            // Kết quả từ MapActivity
            String selectedCity = data.getStringExtra("selected_city");
            if (selectedCity != null && !selectedCity.isEmpty()) {
                fetchWeatherData(selectedCity);
                preferencesHelper.saveLastCity(selectedCity);
            }
        } else if (requestCode == 1003 && resultCode == RESULT_OK) {
            // Kết quả từ SettingsActivity
            if (data != null) {
                String selectedCity = data.getStringExtra("selected_city");
                if (selectedCity != null && !selectedCity.isEmpty()) {
                    fetchWeatherData(selectedCity);
                    preferencesHelper.saveLastCity(selectedCity);
                    return;
                }
            }
            // Nếu không có thành phố được chọn, có thể là đổi đơn vị nhiệt độ
            // Refresh lại dữ liệu để áp dụng đơn vị mới
            if (!currentCity.isEmpty()) {
                fetchWeatherData(currentCity);
            }
        }
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
            Toast.makeText(this, "Vui lòng nhập tên thành phố", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Fetching data for: " + cityName);

        currentCity = cityName;
        binding.searchView.setQuery(cityName, false);

        currentConditionText = null;
        currentDayData = null;
        binding.includeCurrent.tvCurrentTemp.setText("..°");
        binding.includeCurrent.tvCurrentCondition.setText("...");
        binding.includeCurrent.tvCurrentTime.setText("--:--");

        pendingApiCalls = 2; // hourly + daily
        showLoading(true);
        binding.swipeRefreshLayout.setRefreshing(false);

        fetchHourlyWeather(cityName);
        fetchDailyWeather(cityName);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void checkAllApiCallsComplete() {
        pendingApiCalls--;
        if (pendingApiCalls <= 0) {
            showLoading(false);
            binding.swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fetchHourlyWeather(String cityName) {
        weatherService.getHourlyWeather(cityName).enqueue(new Callback<List<HourlyResponse>>() {
            @Override
            public void onResponse(Call<List<HourlyResponse>> call, Response<List<HourlyResponse>> response) {
                checkAllApiCallsComplete();
                
                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu cho thành phố này",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<HourlyResponse> responseList = response.body();
                    
                    // Tìm entry gần nhất với thời gian hiện tại thay vì lấy entry đầu tiên (00:00)
                    HourlyResponse currentHour = findClosestHourlyEntry(responseList);
                    
                    if (currentHour == null) {
                        currentHour = responseList.get(0); // Fallback nếu không tìm được
                    }

                    // Debug: Log values để kiểm tra
                    Log.d(TAG, "Selected hour: " + currentHour.getTime() + ", UV Index: " + currentHour.getUvIndex());
                    Log.d(TAG, "Rain: " + currentHour.getRain() + " mm");

                    hourlyWeatherAdapter.updateData(responseList);
                    // Cập nhật đơn vị nhiệt độ cho adapter
                    hourlyWeatherAdapter.setIsFahrenheit(preferencesHelper.isFahrenheit());
                    
                    // Cập nhật biểu đồ nhiệt độ
                    updateTemperatureChart(responseList);
                    
                    // Tìm vị trí của entry gần nhất với thời gian hiện tại và scroll đến đó
                    int position = findPositionInList(responseList, currentHour);
                    if (position >= 0) {
                        scrollToHourlyPosition(position);
                    }
                    
                    updateWeatherDetailsUI(currentHour);

                    // Hiển thị nhiệt độ với đơn vị đã chọn
                    boolean isFahrenheit = preferencesHelper.isFahrenheit();
                    String tempText = TemperatureUtils.formatTemperature(currentHour.getTemperature(), isFahrenheit);
                    binding.includeCurrent.tvCurrentTemp.setText(tempText.replace("°C", "°").replace("°F", "°"));
                    
                    // Hiển thị icon thời tiết với animation
                    updateWeatherIconWithAnimation(currentHour.getWeather());
                    
                    // Hiển thị giờ hiện tại THỰC TẾ của thiết bị (không phải từ API)
                    String currentTimeStr = formatCurrentRealTime();
                    binding.includeCurrent.tvCurrentTime.setText(currentTimeStr);
                    
                    currentConditionText = currentHour.getWeather();
                    updateMinMaxCondition();
                    
                    // Kiểm tra và hiển thị cảnh báo thời tiết
                    checkWeatherAlerts(currentHour);

                } else {
                    Log.e(TAG, "Error fetching hourly weather data: " + response.message());
                    Toast.makeText(MainActivity.this, "Lỗi máy chủ (hourly): " + response.message(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<HourlyResponse>> call, Throwable t) {
                checkAllApiCallsComplete();
                Log.e(TAG, "Hourly API call failed", t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDailyWeather(String cityName) {
        weatherService.getDailyWeather(cityName).enqueue(new Callback<List<DailyResponse>>() {
            @Override
            public void onResponse(Call<List<DailyResponse>> call, Response<List<DailyResponse>> response) {
                checkAllApiCallsComplete();
                
                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().isEmpty()) {
                        Log.w(TAG, "Daily weather data is empty for " + cityName);
                        return;
                    }

                    List<DailyResponse> responseList = response.body();

                    currentDayData = responseList.get(0);
                    dailyWeatherAdapter.updateData(responseList);
                    // Cập nhật đơn vị nhiệt độ cho adapter
                    dailyWeatherAdapter.setIsFahrenheit(preferencesHelper.isFahrenheit());
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
                checkAllApiCallsComplete();
                Log.e(TAG, "Daily API call failed", t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMinMaxCondition() {
        if (currentConditionText != null && currentDayData != null) {
            boolean isFahrenheit = preferencesHelper.isFahrenheit();
            String minTemp = TemperatureUtils.formatTemperature(currentDayData.getTempMin(), isFahrenheit);
            String maxTemp = TemperatureUtils.formatTemperature(currentDayData.getTempMax(), isFahrenheit);
            minTemp = minTemp.replace("°C", "°").replace("°F", "°");
            maxTemp = maxTemp.replace("°C", "°").replace("°F", "°");
            binding.includeCurrent.tvCurrentCondition.setText(
                    String.format(Locale.getDefault(), "%s  %s / %s",
                            currentConditionText, minTemp, maxTemp));
            currentConditionText = null;
            currentDayData = null;
            
            // Cập nhật widget
            updateWidget();
        }
    }

    private void updateWidget() {
        try {
            // Lấy dữ liệu hiện tại
            String temp = binding.includeCurrent.tvCurrentTemp.getText().toString();
            String condition = binding.includeCurrent.tvCurrentCondition.getText().toString();
            String weather = currentConditionText != null ? currentConditionText : "";
            String minMax = "";
            
            if (currentDayData != null) {
                boolean isFahrenheit = preferencesHelper.isFahrenheit();
                String minTemp = TemperatureUtils.formatTemperature(currentDayData.getTempMin(), isFahrenheit);
                String maxTemp = TemperatureUtils.formatTemperature(currentDayData.getTempMax(), isFahrenheit);
                minMax = minTemp.replace("°C", "°").replace("°F", "°") + " / " + 
                         maxTemp.replace("°C", "°").replace("°F", "°");
            }
            
            // Lưu dữ liệu cho widget
            preferencesHelper.saveWidgetData(temp, condition, weather, minMax);
            
            // Cập nhật widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName componentName = new ComponentName(this, WeatherWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            if (appWidgetIds.length > 0) {
                WeatherWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetIds[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget", e);
        }
    }

    private void updateWeatherDetailsUI(HourlyResponse currentHour) {
        ItemWeatherDetailBinding uv = binding.includeDetails.detailUv;
        ItemWeatherDetailBinding feelsLike = binding.includeDetails.detailFeelsLike;
        ItemWeatherDetailBinding humidity = binding.includeDetails.detailHumidity;
        ItemWeatherDetailBinding wind = binding.includeDetails.detailWind;
        ItemWeatherDetailBinding pressure = binding.includeDetails.detailPressure;
        ItemWeatherDetailBinding visibility = binding.includeDetails.detailVisibility;

        // UV Index - hiển thị giá trị thực tế
        float uvValue = currentHour.getUvIndex();
        uv.tvDetailTitle.setText("UV Index");
        uv.ivDetailIcon.setImageResource(R.drawable.ic_uv_index);
        uv.tvDetailValue.setText(String.format(Locale.getDefault(), "%.2f", uvValue));
        
        // Mô tả UV Index - kiểm tra thời gian thực tế từ API response
        // API trả về thời gian theo múi giờ địa phương của thành phố được query
        // Vì vậy logic này sẽ đúng cho mọi thành phố, không phụ thuộc vào vị trí của người dùng
        String timeStr = currentHour.getTime();
        boolean isNightTime = isNightTime(timeStr);
        
        // Log để debug (có thể bỏ sau khi xác nhận hoạt động đúng)
        Log.d(TAG, "Checking UV for time: " + timeStr + ", isNightTime: " + isNightTime + ", UV: " + uvValue);
        
        if (uvValue == 0) {
            if (isNightTime) {
                uv.tvDetailUnitOrDescription.setText("Ban đêm");
            } else {
                // Ban ngày nhưng UV = 0 (có thể do mây che khuất hoặc sáng sớm/chiều muộn)
                uv.tvDetailUnitOrDescription.setText("Rất thấp");
            }
        } else {
            uv.tvDetailUnitOrDescription.setText(getUvDescription(uvValue));
        }

        // Lượng mưa - hiển thị giá trị thực tế (0 nghĩa là không mưa)
        float rainValue = currentHour.getRain();
        feelsLike.tvDetailTitle.setText("Lượng mưa");
        feelsLike.ivDetailIcon.setImageResource(R.drawable.ic_rainy);
        feelsLike.tvDetailValue.setText(String.format(Locale.getDefault(), "%.1f mm", rainValue));
        
        // Mô tả lượng mưa
        if (rainValue == 0) {
            feelsLike.tvDetailUnitOrDescription.setText("Không mưa");
            feelsLike.tvDetailUnitOrDescription.setVisibility(View.VISIBLE);
        } else if (rainValue < 0.5) {
            feelsLike.tvDetailUnitOrDescription.setText("Mưa nhẹ");
            feelsLike.tvDetailUnitOrDescription.setVisibility(View.VISIBLE);
        } else if (rainValue < 2.5) {
            feelsLike.tvDetailUnitOrDescription.setText("Mưa vừa");
            feelsLike.tvDetailUnitOrDescription.setVisibility(View.VISIBLE);
        } else {
            feelsLike.tvDetailUnitOrDescription.setText("Mưa to");
            feelsLike.tvDetailUnitOrDescription.setVisibility(View.VISIBLE);
        }

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

    /**
     * Kiểm tra xem thời gian có phải là ban đêm không
     * 
     * Lưu ý: API trả về thời gian theo múi giờ địa phương của thành phố được query.
     * Ví dụ: 
     * - Query "Hanoi" → thời gian theo GMT+7 (múi giờ Việt Nam)
     * - Query "New York" → thời gian theo EST/EDT (múi giờ Mỹ)
     * 
     * Logic này sẽ tự động đúng cho mọi thành phố vì sử dụng thời gian từ API response,
     * không phụ thuộc vào vị trí hoặc múi giờ của thiết bị người dùng.
     * 
     * Ban đêm: từ 18:00 đến 6:00 sáng hôm sau (theo múi giờ địa phương của thành phố)
     */
    private boolean isNightTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return false; // Mặc định là ban ngày nếu không parse được
        }
        
        try {
            // Parse time string từ API (ví dụ: "2025-11-06T11:00")
            // Thời gian này đã là local time của thành phố được query
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            int hour = dateTime.getHour();
            
            // Ban đêm: từ 18:00 (18) đến 5:59 (5)
            // Logic này áp dụng cho mọi múi giờ vì dựa trên thời gian local của thành phố
            return hour >= 18 || hour < 6;
        } catch (DateTimeParseException e) {
            Log.w(TAG, "Cannot parse time: " + timeStr, e);
            // Fallback: nếu không parse được, kiểm tra chuỗi có chứa giờ không
            try {
                int tIndex = timeStr.indexOf('T');
                if (tIndex >= 0 && tIndex + 1 < timeStr.length()) {
                    String timePart = timeStr.substring(tIndex + 1);
                    if (timePart.length() >= 2) {
                        int hour = Integer.parseInt(timePart.substring(0, 2));
                        return hour >= 18 || hour < 6;
                    }
                }
            } catch (Exception ex) {
                Log.w(TAG, "Cannot extract hour from time: " + timeStr, ex);
            }
            return false; // Mặc định là ban ngày
        }
    }

    /**
     * Tìm entry dự báo theo giờ gần nhất với thời gian hiện tại.
     * API trả về thời gian theo múi giờ địa phương của thành phố được query.
     * 
     * Logic: Tìm entry có thời gian gần nhất với "bây giờ" (có thể là quá khứ gần hoặc tương lai gần).
     * Ưu tiên entry trong tương lai (dự báo) nếu có, nếu không thì lấy entry quá khứ gần nhất.
     * 
     * @param hourlyList Danh sách các dự báo theo giờ từ API
     * @return Entry gần nhất với thời gian hiện tại
     */
    private HourlyResponse findClosestHourlyEntry(List<HourlyResponse> hourlyList) {
        if (hourlyList == null || hourlyList.isEmpty()) {
            return null;
        }

        // Lấy thời gian hiện tại
        // Lưu ý: API trả về thời gian theo múi giờ địa phương của thành phố
        // Vì vậy ta so sánh với thời gian hiện tại của thiết bị
        // (giả định thiết bị và thành phố có cùng múi giờ hoặc khác biệt không lớn)
        LocalDateTime now = LocalDateTime.now();
        
        HourlyResponse closestFutureEntry = null;
        HourlyResponse closestPastEntry = null;
        long minFutureDiff = Long.MAX_VALUE;
        long minPastDiff = Long.MAX_VALUE;

        for (HourlyResponse entry : hourlyList) {
            try {
                // Parse thời gian từ API (ví dụ: "2025-11-06T14:30")
                LocalDateTime entryDateTime = LocalDateTime.parse(entry.getTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                
                // Tính khoảng cách thời gian (tính bằng phút)
                long diffMinutes = Math.abs(ChronoUnit.MINUTES.between(
                    now.withSecond(0).withNano(0), 
                    entryDateTime.withSecond(0).withNano(0)
                ));
                
                // Phân loại entry: tương lai hoặc quá khứ
                if (entryDateTime.isAfter(now) || entryDateTime.isEqual(now)) {
                    // Entry trong tương lai hoặc hiện tại - ưu tiên
                    if (diffMinutes < minFutureDiff) {
                        minFutureDiff = diffMinutes;
                        closestFutureEntry = entry;
                    }
                } else {
                    // Entry trong quá khứ - chỉ xét nếu không quá 3 giờ
                    if (diffMinutes <= 3 * 60 && diffMinutes < minPastDiff) {
                        minPastDiff = diffMinutes;
                        closestPastEntry = entry;
                    }
                }
            } catch (DateTimeParseException e) {
                Log.w(TAG, "Cannot parse time: " + entry.getTime(), e);
            }
        }
        
        // Ưu tiên entry trong tương lai, nếu không có thì lấy entry quá khứ gần nhất
        HourlyResponse result = closestFutureEntry != null ? closestFutureEntry : 
                                 (closestPastEntry != null ? closestPastEntry : hourlyList.get(0));
        
        Log.d(TAG, "Found closest entry: " + result.getTime() + 
              (closestFutureEntry != null ? " (future)" : " (past)"));

        return result;
    }

    /**
     * Tìm vị trí (index) của một HourlyResponse trong danh sách
     */
    private int findPositionInList(List<HourlyResponse> list, HourlyResponse target) {
        if (list == null || target == null) {
            return -1;
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTime().equals(target.getTime())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Scroll RecyclerView đến vị trí của giờ hiện tại
     * Sử dụng post() để đảm bảo RecyclerView đã render xong
     * Scroll đến vị trí để item hiện tại hiển thị ở đầu (hoặc gần đầu) của list
     */
    private void scrollToHourlyPosition(int position) {
        if (position < 0) {
            return;
        }
        
        // Sử dụng post() với delay nhỏ để đảm bảo RecyclerView đã layout xong
        binding.includeHourly.rvHourlyForecast.postDelayed(() -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.includeHourly.rvHourlyForecast.getLayoutManager();
            if (layoutManager != null) {
                // Scroll đến vị trí, item sẽ hiển thị ở đầu (hoặc gần đầu) của RecyclerView
                // Điều này cho phép user có thể scroll ngược lại để xem các giờ trước đó
                layoutManager.scrollToPositionWithOffset(position, 0);
                
                Log.d(TAG, "Scrolled to position: " + position);
            }
        }, 100); // Delay 100ms để đảm bảo adapter đã render xong
    }

    /**
     * Format thời gian hiện tại THỰC TẾ của thiết bị để hiển thị
     * Ví dụ: "14:30"
     */
    private String formatCurrentRealTime() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
            return now.format(timeFormatter);
        } catch (Exception e) {
            Log.e(TAG, "Cannot format current time", e);
            return "--:--";
        }
    }

    private void updateTemperatureChart(List<HourlyResponse> hourlyList) {
        LineChart chart = binding.includeChart.chartTemperature;
        if (chart == null || hourlyList == null || hourlyList.isEmpty()) {
            return;
        }

        boolean isFahrenheit = preferencesHelper.isFahrenheit();
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Lấy 24 giờ đầu tiên (hoặc tất cả nếu ít hơn 24)
        int maxEntries = Math.min(24, hourlyList.size());
        for (int i = 0; i < maxEntries; i++) {
            HourlyResponse item = hourlyList.get(i);
            double temp = TemperatureUtils.getConvertedTemperature(item.getTemperature(), isFahrenheit);
            entries.add(new Entry(i, (float) temp));
            
            // Format label: chỉ hiển thị giờ
            try {
                LocalDateTime dt = LocalDateTime.parse(item.getTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String timeLabel = dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
                labels.add(timeLabel);
            } catch (Exception e) {
                labels.add(String.valueOf(i));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Nhiệt độ");
        dataSet.setColor(0xFF42A5F5); // Blue accent
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(0xFF42A5F5);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false); // Ẩn giá trị trên điểm để tránh rối
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Làm mượt đường

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Cấu hình chart
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(0x00000000); // Transparent

        // Cấu hình trục X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(0xFFFFFFFF);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(Math.min(maxEntries, 6), true); // Hiển thị tối đa 6 label
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        // Cấu hình trục Y
        chart.getAxisLeft().setTextColor(0xFFFFFFFF);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(0x33FFFFFF);
        chart.getAxisRight().setEnabled(false);

        chart.invalidate(); // Refresh chart
    }

    private void checkWeatherAlerts(HourlyResponse currentHour) {
        if (currentHour == null) return;

        boolean isFahrenheit = preferencesHelper.isFahrenheit();
        double temp = TemperatureUtils.getConvertedTemperature(currentHour.getTemperature(), isFahrenheit);
        double tempThreshold = isFahrenheit ? 86.0 : 30.0; // 30°C = 86°F
        double tempLowThreshold = isFahrenheit ? 50.0 : 10.0; // 10°C = 50°F
        
        // Cảnh báo nhiệt độ quá cao
        if (temp > tempThreshold) {
            String unit = isFahrenheit ? "°F" : "°C";
            WeatherAlertHelper.showAlert(this, 
                "⚠️ Nhiệt độ cao",
                String.format(Locale.getDefault(), 
                    "Nhiệt độ tại %s hiện tại là %.0f%s. Hãy uống nhiều nước và tránh ánh nắng trực tiếp!",
                    currentCity, temp, unit));
        }
        
        // Cảnh báo nhiệt độ quá thấp
        if (temp < tempLowThreshold) {
            String unit = isFahrenheit ? "°F" : "°C";
            WeatherAlertHelper.showAlert(this,
                "🧥 Nhiệt độ thấp",
                String.format(Locale.getDefault(),
                    "Nhiệt độ tại %s hiện tại là %.0f%s. Hãy mặc ấm và giữ gìn sức khỏe!",
                    currentCity, temp, unit));
        }
        
        // Cảnh báo mưa lớn
        if (currentHour.getRain() > 2.5) {
            WeatherAlertHelper.showAlert(this,
                "🌧️ Mưa lớn",
                String.format(Locale.getDefault(),
                    "Lượng mưa tại %s là %.1f mm. Hãy cẩn thận khi đi đường và mang theo ô!",
                    currentCity, currentHour.getRain()));
        }
        
        // Cảnh báo gió mạnh
        if (currentHour.getWindSpeed() > 30) {
            WeatherAlertHelper.showAlert(this,
                "💨 Gió mạnh",
                String.format(Locale.getDefault(),
                    "Tốc độ gió tại %s là %.0f km/h. Hãy cẩn thận khi ra ngoài!",
                    currentCity, currentHour.getWindSpeed()));
        }
    }

    private void updateWeatherIconWithAnimation(String weather) {
        if (binding.includeCurrent.ivCurrentWeatherIcon == null || weather == null) {
            return;
        }

        // Dừng animation cũ nếu có
        binding.includeCurrent.ivCurrentWeatherIcon.clearAnimation();

        // Lấy icon resource
        int iconRes = getWeatherIconResource(weather);
        binding.includeCurrent.ivCurrentWeatherIcon.setImageResource(iconRes);

        // Apply animation fade in
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.weather_icon_fade_in);
        binding.includeCurrent.ivCurrentWeatherIcon.startAnimation(fadeIn);

        // Apply animation theo loại thời tiết
        String weatherLower = weather.toLowerCase(Locale.ROOT);
        if (weatherLower.contains("trời quang") || weatherLower.contains("nắng")) {
            // Animation xoay cho mặt trời
            Animation rotate = AnimationUtils.loadAnimation(this, R.anim.sun_rotate);
            binding.includeCurrent.ivCurrentWeatherIcon.startAnimation(rotate);
        } else if (weatherLower.contains("có mây") || weatherLower.contains("nhiều mây")) {
            // Animation float cho mây
            Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.cloud_float);
            binding.includeCurrent.ivCurrentWeatherIcon.startAnimation(floatAnim);
        }
    }

    private int getWeatherIconResource(String weather) {
        if (weather == null) return R.drawable.ic_cloudy;
        
        String weatherLower = weather.toLowerCase(Locale.ROOT);
        
        if (weatherLower.contains("trời quang") || weatherLower.contains("nắng")) {
            return R.drawable.ic_sun;
        } else if (weatherLower.contains("có mây") || weatherLower.contains("dễ chịu")) {
            return R.drawable.ic_cloudy;
        } else if (weatherLower.contains("mưa to") || weatherLower.contains("mưa rào to")) {
            return R.drawable.ic_heavy_rain;
        } else if (weatherLower.contains("mưa")) {
            return R.drawable.ic_rainy;
        } else if (weatherLower.contains("dông") || weatherLower.contains("sấm chớp")) {
            return R.drawable.ic_storm;
        } else if (weatherLower.contains("tuyết")) {
            return R.drawable.ic_snowy;
        } else if (weatherLower.contains("sương mù")) {
            return R.drawable.ic_fog;
        }
        
        return R.drawable.ic_cloudy;
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
