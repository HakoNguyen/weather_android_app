package com.example.weather_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.ViewHolder> {

    private List<HourlyResponse> hourlyWeatherList;
    private boolean isFahrenheit;

    public HourlyWeatherAdapter(List<HourlyResponse> hourlyWeatherList) {
        this.hourlyWeatherList = hourlyWeatherList;
        this.isFahrenheit = false; // Default
    }

    public void setIsFahrenheit(boolean isFahrenheit) {
        this.isFahrenheit = isFahrenheit;
        notifyDataSetChanged();
    }

    public void updateData(List<HourlyResponse> hourlyWeatherList) {
        this.hourlyWeatherList.clear();
        this.hourlyWeatherList.addAll(hourlyWeatherList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyResponse item = hourlyWeatherList.get(position);
        holder.tvTime.setText(formatTime(item.getTime()));
        
        // Sử dụng TemperatureUtils để format nhiệt độ
        String tempText = TemperatureUtils.formatTemperature(item.getTemperature(), isFahrenheit);
        tempText = tempText.replace("°C", "°").replace("°F", "°");
        holder.tvTemp.setText(tempText);

        holder.ivIcon.setImageResource(getWeatherIcon(item.getWeather()));
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty())
            return "--:--";

        // Try parse as LocalDateTime (ISO_LOCAL_DATE_TIME like 2025-11-04T00:00)
        try {
            LocalDateTime dt = LocalDateTime.parse(isoTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault());

            String timePart = dt.format(timeFmt);
            // If the date differs from today, append date on a second line (keeps compact)
            if (!dt.toLocalDate().equals(LocalDate.now())) {
                return String.format(Locale.getDefault(), "%s\n%s", timePart, dt.format(dateFmt));
            }
            return timePart;
        } catch (DateTimeParseException ignored) {
            // fallback: attempt to handle ISO with zone or just extract after 'T'
            try {
                int tIndex = isoTime.indexOf('T');
                if (tIndex >= 0 && tIndex + 1 < isoTime.length()) {
                    String afterT = isoTime.substring(tIndex + 1);
                    // If contains seconds, trim to HH:mm
                    if (afterT.length() >= 5)
                        return afterT.substring(0, 5);
                    return afterT;
                }
            } catch (Exception e) {
                // ignore and fallthrough
            }
        }
        return isoTime;
    }

    @Override
    public int getItemCount() {
        return (hourlyWeatherList != null) ? hourlyWeatherList.size() : 0;
    }

    private int getWeatherIcon(String weatherCondition) {
        if (weatherCondition == null)
            return R.drawable.ic_cloudy;

        weatherCondition = weatherCondition.toLowerCase(Locale.ROOT);

        String[] sunKeyWords = { "trời quang", "nắng" };
        String[] sunCloudKeyWords = { "gần như quang", "có mây", "dễ chịu" };
        String[] fogKeyWords = { "nhiều mây", "sương mù", "sương muối" };
        String[] rainKeyWords = { "mưa phùn nhẹ", "mưa phùn vừa", "mưa nhỏ", "mưa rào nhẹ" };
        String[] heavyRainKeyWords = { "mưa phùn dày", "mưa vừa", "mưa to", "mưa rào to" };
        String[] snowKeyWords = { "tuyết", "mưa đóng băng" };
        String[] stormKeyWords = { "dông", "sấm chớp", "bão" };

        if (containsAny(weatherCondition, stormKeyWords))
            return R.drawable.ic_storm;
        if (containsAny(weatherCondition, snowKeyWords))
            return R.drawable.ic_snowy;
        if (containsAny(weatherCondition, heavyRainKeyWords))
            return R.drawable.ic_heavy_rain;
        if (containsAny(weatherCondition, rainKeyWords))
            return R.drawable.ic_rainy;
        if (containsAny(weatherCondition, fogKeyWords))
            return R.drawable.ic_fog;
        if (containsAny(weatherCondition, sunCloudKeyWords))
            return R.drawable.ic_cloudy;
        if (containsAny(weatherCondition, sunKeyWords))
            return R.drawable.ic_sun;

        return R.drawable.ic_cloudy;
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvHourlyTime);
            tvTemp = itemView.findViewById(R.id.tvHourlyTemp);
            ivIcon = itemView.findViewById(R.id.ivHourlyIcon);
        }
    }
}
