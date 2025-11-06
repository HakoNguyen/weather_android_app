package com.example.weather_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteCitiesAdapter extends RecyclerView.Adapter<FavoriteCitiesAdapter.ViewHolder> {
    private List<String> cities;
    private List<String> citiesFiltered;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(String cityName);
        void onCityDelete(String cityName);
    }

    public FavoriteCitiesAdapter(List<String> cities, OnCityClickListener listener) {
        this.cities = new ArrayList<>(cities);
        this.citiesFiltered = new ArrayList<>(cities);
        this.listener = listener;
    }

    public void filter(String query) {
        citiesFiltered.clear();
        if (query == null || query.trim().isEmpty()) {
            citiesFiltered.addAll(cities);
        } else {
            String queryLower = query.toLowerCase(Locale.getDefault());
            for (String city : cities) {
                if (city.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    citiesFiltered.add(city);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String cityName = citiesFiltered.get(position);
        holder.tvCityName.setText(cityName);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(cityName);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityDelete(cityName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return citiesFiltered.size();
    }

    public void updateData(List<String> newCities) {
        this.cities.clear();
        this.cities.addAll(newCities);
        this.citiesFiltered.clear();
        this.citiesFiltered.addAll(newCities);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

