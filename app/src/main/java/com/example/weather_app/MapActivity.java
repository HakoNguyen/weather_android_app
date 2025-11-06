package com.example.weather_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.weather_app.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.Locale;

import androidx.appcompat.widget.SearchView;

public class MapActivity extends AppCompatActivity implements MapEventsReceiver {
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    
    private ActivityMapBinding binding;
    private MapView mapView;
    private IMapController mapController;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentMarker;
    private LocationHelper locationHelper;
    private MyLocationNewOverlay myLocationOverlay;
    private GeoPoint selectedLocation;
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Cấu hình OSMDroid
        setupOSMDroid();
        
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy thành phố hiện tại từ Intent
        currentCity = getIntent().getStringExtra("current_city");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationHelper = new LocationHelper(this);

        setupToolbar();
        setupMap();
        setupButton();
        setupSearchView();
        
        // Hiển thị vị trí của thành phố hiện tại (nếu có)
        if (currentCity != null && !currentCity.isEmpty()) {
            showCurrentCityLocation();
        }
    }

    private void setupOSMDroid() {
        // Cấu hình OSMDroid để cache tiles
        Configuration.getInstance().setUserAgentValue(getPackageName());
        File osmdroidBasePath = new File(getCacheDir(), "osmdroid");
        osmdroidBasePath.mkdirs();
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);
        
        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        osmdroidTileCache.mkdirs();
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTileCache);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chọn vị trí trên bản đồ");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMap() {
        mapView = binding.map;
        
        // Cấu hình map với khả năng zoom tốt hơn
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Sử dụng OpenStreetMap tiles
        mapView.setMultiTouchControls(true); // Pinch-to-zoom
        mapView.setBuiltInZoomControls(true); // Nút zoom +/-
        mapView.setMinZoomLevel(3.0); // Zoom out xa (toàn cầu)
        mapView.setMaxZoomLevel(21.0); // Zoom in rất chi tiết (tương đương Google Maps)
        mapView.getController().setZoom(12.0); // Mức zoom mặc định
        
        mapController = mapView.getController();
        
        // Thêm overlay để bắt sự kiện click trên bản đồ
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        mapView.getOverlays().add(mapEventsOverlay);
        
        // Không tự động gọi GPS, chỉ setup overlay nếu có quyền
        // Vị trí hiện tại sẽ được hiển thị từ currentCity (nếu có)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupMyLocationOverlay();
        }
    }

    private void showCurrentCityLocation() {
        // Lấy tọa độ từ tên thành phố hiện tại
        locationHelper.getCoordinatesFromCityName(currentCity, new LocationHelper.CoordinatesCallback() {
            @Override
            public void onCoordinatesReceived(double latitude, double longitude) {
                GeoPoint cityGeoPoint = new GeoPoint(latitude, longitude);
                selectedLocation = cityGeoPoint;
                placeMarker(cityGeoPoint);
                
                // Hiển thị tên thành phố
                binding.tvSelectedLocation.setText("Vị trí: " + currentCity);
                binding.btnGetWeather.setEnabled(true);
            }

            @Override
            public void onCoordinatesFailure(String error) {
                // Nếu không tìm thấy tọa độ, vẫn hiển thị tên thành phố
                binding.tvSelectedLocation.setText("Vị trí: " + currentCity);
                binding.btnGetWeather.setEnabled(true);
            }
        });
    }

    private void setupMyLocationOverlay() {
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
        }
    }

    private void setupButton() {
        binding.btnSelectLocation.setOnClickListener(v -> selectCurrentLocation());
        binding.btnGetWeather.setOnClickListener(v -> getWeatherForSelectedLocation());
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    searchCity(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchCity(String cityName) {
        // Hiển thị loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.searchView.clearFocus();
        
        // Tìm kiếm thành phố bằng Geocoding
        locationHelper.getCoordinatesFromCityName(cityName, new LocationHelper.CoordinatesCallback() {
            @Override
            public void onCoordinatesReceived(double latitude, double longitude) {
                binding.progressBar.setVisibility(View.GONE);
                
                // Di chuyển map đến vị trí tìm được
                GeoPoint cityGeoPoint = new GeoPoint(latitude, longitude);
                selectedLocation = cityGeoPoint;
                placeMarker(cityGeoPoint);
                
                // Hiển thị tên thành phố
                binding.tvSelectedLocation.setText("Vị trí: " + cityName);
                binding.btnGetWeather.setEnabled(true);
                
                // Zoom in một chút để thấy rõ hơn
                mapController.animateTo(cityGeoPoint);
                mapController.setZoom(14.0);
            }

            @Override
            public void onCoordinatesFailure(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MapActivity.this, "Không tìm thấy thành phố: " + cityName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        // Khi click vào bản đồ, đặt marker tại vị trí đó
        selectedLocation = p;
        placeMarker(p);
        
        // Lấy tên thành phố từ tọa độ
        locationHelper.getCityNameFromCoordinates(p.getLatitude(), p.getLongitude(), new LocationHelper.CityNameCallback() {
            @Override
            public void onCityNameReceived(String cityName) {
                if (cityName != null) {
                    binding.tvSelectedLocation.setText("Vị trí: " + cityName);
                    binding.btnGetWeather.setEnabled(true);
                } else {
                    binding.tvSelectedLocation.setText("Vị trí: " + String.format(Locale.getDefault(), "%.4f, %.4f", 
                            p.getLatitude(), p.getLongitude()));
                    binding.btnGetWeather.setEnabled(true);
                }
            }
        });
        
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        // Không cần xử lý long press
        return false;
    }

    private void placeMarker(GeoPoint geoPoint) {
        // Xóa marker cũ nếu có
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }
        
        // Tạo marker mới
        currentMarker = new Marker(mapView);
        currentMarker.setPosition(geoPoint);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setTitle("Vị trí được chọn");
        mapView.getOverlays().add(currentMarker);
        
        // Di chuyển camera đến vị trí marker
        mapController.animateTo(geoPoint);
        mapController.setZoom(15.0);
        
        mapView.invalidate();
    }

    private void moveToCurrentLocation() {
        // Kiểm tra quyền trước
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        // Hiển thị loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSelectLocation.setEnabled(false);

        Task<Location> locationTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
        );

        locationTask.addOnSuccessListener(location -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSelectLocation.setEnabled(true);
            
            if (location != null && mapView != null) {
                GeoPoint currentGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                selectedLocation = currentGeoPoint;
                placeMarker(currentGeoPoint);
                
                // Lấy tên thành phố từ GPS và hiển thị
                locationHelper.getCityNameFromCoordinates(location.getLatitude(), location.getLongitude(), 
                        new LocationHelper.CityNameCallback() {
                    @Override
                    public void onCityNameReceived(String cityName) {
                        if (cityName != null) {
                            binding.tvSelectedLocation.setText("Vị trí: " + cityName);
                            binding.btnGetWeather.setEnabled(true);
                        } else {
                            binding.tvSelectedLocation.setText("Vị trí: " + String.format(Locale.getDefault(), 
                                    "%.4f, %.4f", location.getLatitude(), location.getLongitude()));
                            binding.btnGetWeather.setEnabled(true);
                        }
                    }
                });
            } else {
                Toast.makeText(MapActivity.this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSelectLocation.setEnabled(true);
            Toast.makeText(MapActivity.this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void selectCurrentLocation() {
        moveToCurrentLocation();
    }

    private void getWeatherForSelectedLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
            return;
        }

        locationHelper.getCityNameFromCoordinates(selectedLocation.getLatitude(), selectedLocation.getLongitude(), 
                new LocationHelper.CityNameCallback() {
            @Override
            public void onCityNameReceived(String cityName) {
                if (cityName != null) {
                    getWeatherForCity(cityName);
                } else {
                    Toast.makeText(MapActivity.this, "Không thể lấy tên thành phố", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getWeatherForCity(String cityName) {
        // Trả về thành phố được chọn và quay về MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_city", cityName);
        setResult(RESULT_OK, resultIntent);
        
        // Hiển thị loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGetWeather.setEnabled(false);
        
        // Quay về ngay sau khi user bấm nút
        binding.btnGetWeather.postDelayed(() -> {
            finish();
        }, 300);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Cần quyền vị trí để hiển thị bản đồ", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMyLocationOverlay();
                // Không tự động gọi moveToCurrentLocation(), chỉ khi user bấm nút
            } else {
                Toast.makeText(this, "Cần quyền vị trí để sử dụng tính năng vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
