package com.example.myweather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder> {

    private final List<HourlyData> hourlyDataList;
    private final Context context;

    public HourlyForecastAdapter(Context context, List<HourlyData> hourlyDataList) {
        this.context = context;
        this.hourlyDataList = hourlyDataList;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hourly_forecast_item, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        HourlyData data = hourlyDataList.get(position);
        holder.tvHour.setText(data.getTime());
        holder.ivWeatherIcon.setImageResource(data.getIcon());
        holder.tvTemperature.setText(data.getTemperature());
    }

    @Override
    public int getItemCount() {
        return hourlyDataList.size();
    }

    // Lớp ViewHolder
    public static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour, tvTemperature;
        ImageView ivWeatherIcon;

        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
    }
}
