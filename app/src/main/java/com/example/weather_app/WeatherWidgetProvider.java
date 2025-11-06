package com.example.weather_app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.weather_app.databinding.ActivityMainBinding;

import java.util.Locale;

public class WeatherWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WeatherWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Lấy dữ liệu từ SharedPreferences
        PreferencesHelper preferencesHelper = new PreferencesHelper(context);
        String lastCity = preferencesHelper.getLastCity();
        boolean isFahrenheit = preferencesHelper.isFahrenheit();

        // Lấy dữ liệu thời tiết đã lưu (nếu có)
        SharedPreferences prefs = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE);
        String currentTemp = prefs.getString("widget_temp", "--");
        String currentCondition = prefs.getString("widget_condition", "...");
        String minMaxTemp = prefs.getString("widget_minmax", "-- / --");

        // Tạo RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);

        // Cập nhật thông tin
        views.setTextViewText(R.id.widget_city, lastCity);
        views.setTextViewText(R.id.widget_temp, currentTemp);
        views.setTextViewText(R.id.widget_condition, currentCondition);
        views.setTextViewText(R.id.widget_minmax, minMaxTemp);

        // Set icon thời tiết
        String weather = prefs.getString("widget_weather", "");
        if (weather != null && !weather.isEmpty()) {
            int iconRes = getWeatherIconRes(weather);
            views.setImageViewResource(R.id.widget_icon, iconRes);
        }

        // Click vào widget sẽ mở app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        // Cập nhật widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getWeatherIconRes(String weather) {
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

    @Override
    public void onEnabled(Context context) {
        // Widget được thêm vào home screen lần đầu
    }

    @Override
    public void onDisabled(Context context) {
        // Widget cuối cùng bị xóa
    }
}

