package com.example.weather_app;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.example.weather_app.databinding.ActivitySettingsBinding;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private PreferencesHelper preferencesHelper;
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencesHelper = new PreferencesHelper(this);
        currentCity = getIntent().getStringExtra("current_city");

        setupToolbar();
        setupSettings();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cài đặt");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSettings() {
        // Cài đặt tự động lấy vị trí
        boolean useGps = preferencesHelper.shouldUseGps();
        binding.switchAutoLocation.setChecked(useGps);
        binding.switchAutoLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesHelper.setUseGps(isChecked);
            Toast.makeText(this, isChecked ? "Đã bật tự động lấy vị trí" : "Đã tắt tự động lấy vị trí", 
                    Toast.LENGTH_SHORT).show();
        });

        // Cài đặt thành phố yêu thích
        updateFavoriteButton();
        binding.btnFavorite.setOnClickListener(v -> toggleFavoriteCity());

        // Cài đặt đơn vị nhiệt độ
        setupTemperatureUnit();

        // Quản lý thành phố yêu thích
        List<String> favoriteCities = preferencesHelper.getFavoriteCities();
        binding.btnManageFavorites.setText("Quản lý thành phố yêu thích (" + favoriteCities.size() + ")");
        binding.btnManageFavorites.setOnClickListener(v -> openFavoriteCitiesActivity());
    }

    private void setupTemperatureUnit() {
        boolean isFahrenheit = preferencesHelper.isFahrenheit();
        binding.switchTemperatureUnit.setChecked(isFahrenheit);
        binding.tvTemperatureUnit.setText(isFahrenheit ? "°F" : "°C");
        
        binding.switchTemperatureUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String unit = isChecked ? "F" : "C";
            preferencesHelper.setTemperatureUnit(unit);
            binding.tvTemperatureUnit.setText(isChecked ? "°F" : "°C");
            Toast.makeText(this, "Đã chuyển sang " + (isChecked ? "Fahrenheit" : "Celsius"), 
                    Toast.LENGTH_SHORT).show();
            
            // Thông báo MainActivity refresh để áp dụng đơn vị mới
            setResult(RESULT_OK);
        });
    }

    private void updateFavoriteButton() {
        if (currentCity == null || currentCity.isEmpty()) {
            binding.btnFavorite.setVisibility(View.GONE);
            return;
        }

        binding.btnFavorite.setVisibility(View.VISIBLE);
        boolean isFavorite = preferencesHelper.isFavoriteCity(currentCity);
        
        if (isFavorite) {
            binding.btnFavorite.setText("Xóa khỏi yêu thích");
            binding.ivFavoriteIcon.setImageResource(android.R.drawable.star_big_on);
        } else {
            binding.btnFavorite.setText("Thêm vào yêu thích");
            binding.ivFavoriteIcon.setImageResource(android.R.drawable.star_big_off);
        }
        // Set màu cho icon
        binding.ivFavoriteIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.blue_accent),
                PorterDuff.Mode.SRC_IN);
    }

    private void toggleFavoriteCity() {
        if (currentCity == null || currentCity.isEmpty()) {
            Toast.makeText(this, "Chưa có thành phố được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFavorite = preferencesHelper.isFavoriteCity(currentCity);
        if (isFavorite) {
            preferencesHelper.removeFavoriteCity(currentCity);
            Toast.makeText(this, "Đã xóa " + currentCity + " khỏi yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            preferencesHelper.addFavoriteCity(currentCity);
            Toast.makeText(this, "Đã thêm " + currentCity + " vào yêu thích", Toast.LENGTH_SHORT).show();
        }
        
        updateFavoriteButton();
        
        // Cập nhật số lượng trong nút quản lý
        List<String> favoriteCities = preferencesHelper.getFavoriteCities();
        binding.btnManageFavorites.setText("Quản lý thành phố yêu thích (" + favoriteCities.size() + ")");
    }

    private void openFavoriteCitiesActivity() {
        Intent intent = new Intent(this, FavoriteCitiesActivity.class);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Nếu có thành phố được chọn từ FavoriteCitiesActivity
            String selectedCity = data.getStringExtra("selected_city");
            if (selectedCity != null && !selectedCity.isEmpty()) {
                // Chuyển tiếp kết quả về MainActivity và quay về
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_city", selectedCity);
                setResult(RESULT_OK, resultIntent);
                finish(); // Quay về MainActivity
                return;
            }
            
            // Nếu không có thành phố được chọn, chỉ cập nhật số lượng
            List<String> favoriteCities = preferencesHelper.getFavoriteCities();
            binding.btnManageFavorites.setText("Quản lý thành phố yêu thích (" + favoriteCities.size() + ")");
        }
    }
}

