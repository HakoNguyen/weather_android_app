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

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder> {

    private final List<DailyData> dailyDataList;
    private final Context context;

    public DailyForecastAdapter(Context context, List<DailyData> dailyDataList) {
        this.context = context;
        this.dailyDataList = dailyDataList;
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daily_forecast_item, parent, false);
        return new DailyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        DailyData data = dailyDataList.get(position);
        holder.tvDay.setText(data.getDay());
        holder.ivWeatherIcon.setImageResource(data.getIcon());
        holder.tvTempRange.setText(data.getTempRange());
    }

    @Override
    public int getItemCount() {
        return dailyDataList.size();
    }

    // Lớp ViewHolder
    public static class DailyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTempRange;
        ImageView ivWeatherIcon;

        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
    }
}
