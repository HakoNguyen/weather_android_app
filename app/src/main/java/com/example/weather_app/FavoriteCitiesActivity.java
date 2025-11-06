package com.example.weather_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather_app.databinding.ActivityFavoriteCitiesBinding;

import java.util.ArrayList;
import java.util.List;

public class FavoriteCitiesActivity extends AppCompatActivity {
    private ActivityFavoriteCitiesBinding binding;
    private PreferencesHelper preferencesHelper;
    private FavoriteCitiesAdapter adapter;
    private List<String> favoriteCities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteCitiesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencesHelper = new PreferencesHelper(this);

        setupToolbar();
        setupRecyclerView();
        setupSearchView();
        loadFavoriteCities();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thành phố yêu thích");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new FavoriteCitiesAdapter(favoriteCities, new FavoriteCitiesAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(String cityName) {
                // Trả về thành phố được chọn
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_city", cityName);
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onCityDelete(String cityName) {
                preferencesHelper.removeFavoriteCity(cityName);
                loadFavoriteCities();
                Toast.makeText(FavoriteCitiesActivity.this, "Đã xóa " + cityName, Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        
        // Thêm divider giữa các item
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.recyclerView.addItemDecoration(divider);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void loadFavoriteCities() {
        favoriteCities.clear();
        favoriteCities.addAll(preferencesHelper.getFavoriteCities());
        adapter.updateData(favoriteCities);
        
        // Hiển thị empty state nếu không có thành phố yêu thích
        if (favoriteCities.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }
}

