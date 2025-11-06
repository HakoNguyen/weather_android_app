package com.example.weather_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder> {
    private List<DailyResponse> dailyWeatherList;
    private boolean isFahrenheit;

    public DailyWeatherAdapter(List<DailyResponse> dailyWeatherList) {
        this.dailyWeatherList = dailyWeatherList;
        this.isFahrenheit = false; // Default
    }

    public void setIsFahrenheit(boolean isFahrenheit) {
        this.isFahrenheit = isFahrenheit;
        notifyDataSetChanged();
    }
    public void updateData(List<DailyResponse> newDailyWeatherList) {
        if (this.dailyWeatherList != null) {
        this.dailyWeatherList.clear();
        this.dailyWeatherList.addAll(newDailyWeatherList);
        notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyResponse item = dailyWeatherList.get(position);

        holder.tvDate.setText(item.getDate());

        // Sử dụng TemperatureUtils để format nhiệt độ
        String minTemp = TemperatureUtils.formatTemperature(item.getTempMin(), isFahrenheit);
        String maxTemp = TemperatureUtils.formatTemperature(item.getTempMax(), isFahrenheit);
        minTemp = minTemp.replace("°C", "°").replace("°F", "°");
        maxTemp = maxTemp.replace("°C", "°").replace("°F", "°");
        holder.tvTempRange.setText(String.format(Locale.getDefault(), "%s / %s", minTemp, maxTemp));

        holder.ivIcon.setImageResource(getWeatherIcon(item.getWeather()));


    }

    @Override
    public int getItemCount() {
        return (dailyWeatherList != null) ? dailyWeatherList.size() : 0;
    }

    private int getWeatherIcon(String weatherCondition) {
        if (weatherCondition == null) return R.drawable.ic_cloudy;
        weatherCondition = weatherCondition.toLowerCase(Locale.ROOT);

        String[] sunKeyWords = {"trời quang", "nắng"};
        String[] sunCloudKeyWords = {"gần như quang", "có mây", "dễ chịu"};
        String[] fogKeyWords = {"nhiều mây", "sương mù", "sương muối"};
        String[] rainKeyWords = {"mưa phùn nhẹ", "mưa phùn vừa", "mưa nhỏ", "mưa rào nhẹ"};
        String[] heavyRainKeyWords = {"mưa phùn dày", "mưa vừa", "mưa to", "mưa rào to"};
        String[] snowKeyWords = {"tuyết", "mưa đóng băng"};
        String[] stormKeyWords = {"dông", "sấm chớp", "bão"};

        if (containsAny(weatherCondition, stormKeyWords))
            return R.drawable.ic_storm;
        if (containsAny(weatherCondition, snowKeyWords))
            return R.drawable.ic_snowy;
        if (containsAny(weatherCondition, heavyRainKeyWords))
            return R.drawable.ic_heavy_rain;
        if (containsAny(weatherCondition, rainKeyWords))
            return R.drawable.ic_rain;
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
        TextView tvDate, tvTempRange, tvPrecipitation;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDailyDate);
            tvTempRange = itemView.findViewById(R.id.tvDailyTempRange);
            ivIcon = itemView.findViewById(R.id.ivDailyIcon);
        }
    }
}
