package com.example.weather_app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferencesHelper {
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_LAST_CITY = "last_city";
    private static final String KEY_FAVORITE_CITIES = "favorite_cities";
    private static final String KEY_USE_GPS = "use_gps";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit"; // "C" or "F"

    private SharedPreferences sharedPreferences;

    public PreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLastCity(String cityName) {
        sharedPreferences.edit().putString(KEY_LAST_CITY, cityName).apply();
    }

    public String getLastCity() {
        return sharedPreferences.getString(KEY_LAST_CITY, "Hanoi");
    }

    public void addFavoriteCity(String cityName) {
        Set<String> favorites = getFavoriteCitiesSet();
        favorites.add(cityName);
        sharedPreferences.edit().putStringSet(KEY_FAVORITE_CITIES, favorites).apply();
    }

    public void removeFavoriteCity(String cityName) {
        Set<String> favorites = getFavoriteCitiesSet();
        favorites.remove(cityName);
        sharedPreferences.edit().putStringSet(KEY_FAVORITE_CITIES, favorites).apply();
    }

    public List<String> getFavoriteCities() {
        Set<String> favorites = getFavoriteCitiesSet();
        return new ArrayList<>(favorites);
    }

    public boolean isFavoriteCity(String cityName) {
        return getFavoriteCitiesSet().contains(cityName);
    }

    private Set<String> getFavoriteCitiesSet() {
        Set<String> favorites = sharedPreferences.getStringSet(KEY_FAVORITE_CITIES, null);
        if (favorites == null) {
            favorites = new HashSet<>();
        }
        return new HashSet<>(favorites);
    }

    public void setUseGps(boolean useGps) {
        sharedPreferences.edit().putBoolean(KEY_USE_GPS, useGps).apply();
    }

    public boolean shouldUseGps() {
        return sharedPreferences.getBoolean(KEY_USE_GPS, false);
    }

    public void setTemperatureUnit(String unit) {
        sharedPreferences.edit().putString(KEY_TEMPERATURE_UNIT, unit).apply();
    }

    public String getTemperatureUnit() {
        return sharedPreferences.getString(KEY_TEMPERATURE_UNIT, "C"); // Mặc định là Celsius
    }

    public boolean isFahrenheit() {
        return "F".equals(getTemperatureUnit());
    }

    // Widget data methods
    public void saveWidgetData(String temp, String condition, String weather, String minMax) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("widget_temp", temp);
        editor.putString("widget_condition", condition);
        editor.putString("widget_weather", weather);
        editor.putString("widget_minmax", minMax);
        editor.apply();
    }

    public void clearWidgetData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("widget_temp");
        editor.remove("widget_condition");
        editor.remove("widget_weather");
        editor.remove("widget_minmax");
        editor.apply();
    }
}
