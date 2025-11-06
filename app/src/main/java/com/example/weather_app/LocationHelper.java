package com.example.weather_app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private static final String TAG = "LocationHelper";
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public interface LocationCallback {
        void onLocationSuccess(String cityName);
        void onLocationFailure(String error);
    }

    public void getCurrentCity(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationFailure("Location permission not granted");
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
        );

        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                String cityName = getCityNameFromLocation(location.getLatitude(), location.getLongitude());
                if (cityName != null) {
                    callback.onLocationSuccess(cityName);
                } else {
                    callback.onLocationFailure("Could not get city name from location");
                }
            } else {
                callback.onLocationFailure("Location is null");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get location", e);
            callback.onLocationFailure("Failed to get location: " + e.getMessage());
        });
    }

    private String getCityNameFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Lấy tên thành phố, nếu không có thì lấy locality
                String city = address.getLocality();
                if (city == null || city.isEmpty()) {
                    city = address.getAdminArea();
                }
                return city;
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
        }
        return null;
    }

    public interface CityNameCallback {
        void onCityNameReceived(String cityName);
    }

    public void getCityNameFromCoordinates(double latitude, double longitude, CityNameCallback callback) {
        // Chạy trên background thread để tránh block UI
        new Thread(() -> {
            String cityName = getCityNameFromLocation(latitude, longitude);
            if (callback != null) {
                // Chạy callback trên main thread
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.post(() -> callback.onCityNameReceived(cityName));
            }
        }).start();
    }

    public interface CoordinatesCallback {
        void onCoordinatesReceived(double latitude, double longitude);
        void onCoordinatesFailure(String error);
    }

    public void getCoordinatesFromCityName(String cityName, CoordinatesCallback callback) {
        // Chạy trên background thread để tránh block UI
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();
                    if (callback != null) {
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.post(() -> callback.onCoordinatesReceived(latitude, longitude));
                    }
                } else {
                    if (callback != null) {
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.post(() -> callback.onCoordinatesFailure("Không tìm thấy tọa độ cho " + cityName));
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder error", e);
                if (callback != null) {
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(() -> callback.onCoordinatesFailure("Lỗi khi tìm tọa độ: " + e.getMessage()));
                }
            }
        }).start();
    }

    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
