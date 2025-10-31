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

public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.ViewHolder> {

    private List<HourlyResponse> hourlyWeatherList;
    public HourlyWeatherAdapter(List<HourlyResponse> hourlyWeatherList) {
        this.hourlyWeatherList = hourlyWeatherList;
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

        holder.tvTime.setText(item.getTime());
        holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.getTemperature()));


        holder.ivIcon.setImageResource(getWeatherIcon(item.getWeather()));
    }

    @Override
    public int getItemCount() {
        return (hourlyWeatherList != null) ? hourlyWeatherList.size() : 0;
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
