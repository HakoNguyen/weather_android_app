package com.example.myweather;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView hourlyForecastRecyclerView;
    private RecyclerView dailyForecastRecyclerView;
    private HourlyForecastAdapter hourlyAdapter;
    private DailyForecastAdapter dailyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hourlyForecastRecyclerView = findViewById(R.id.hourlyForecastRecyclerView);
        dailyForecastRecyclerView = findViewById(R.id.dailyForecastRecyclerView);

        setupHourlyForecast();
        setupDailyForecast();
    }

    private void setupHourlyForecast() {
        hourlyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Dữ liệu giả cho dự báo hàng giờ
        List<HourlyData> hourlyData = new ArrayList<>();
        hourlyData.add(new HourlyData("Bây giờ", R.drawable.ic_cloudy_day, "25°"));
        hourlyData.add(new HourlyData("23:00", R.drawable.ic_cloudy_day, "24°"));
        hourlyData.add(new HourlyData("0:00", R.drawable.ic_cloudy_day, "24°"));
        hourlyData.add(new HourlyData("1:00", R.drawable.ic_cloudy, "23°"));
        hourlyData.add(new HourlyData("2:00", R.drawable.ic_cloudy, "22°"));
        hourlyData.add(new HourlyData("3:00", R.drawable.ic_night, "23°"));

        // Khởi tạo và set adapter
        hourlyAdapter = new HourlyForecastAdapter(this, hourlyData);
        hourlyForecastRecyclerView.setAdapter(hourlyAdapter);
    }

    private void setupDailyForecast() {
        dailyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu giả cho dự báo hàng ngày
        List<DailyData> dailyData = new ArrayList<>();
        dailyData.add(new DailyData("26 thg 10 Hôm nay", R.drawable.ic_sunny, "22° / 28°"));
        dailyData.add(new DailyData("27 thg 10 Ngày mai", R.drawable.ic_cloudy_day, "21° / 27°"));
        dailyData.add(new DailyData("28 thg 10 Thứ 3", R.drawable.ic_cloudy, "20° / 27°"));
        dailyData.add(new DailyData("29 thg 10 Thứ 4", R.drawable.ic_cloudy, "21° / 27°"));
        dailyData.add(new DailyData("30 thg 10 Thứ 5", R.drawable.ic_rain, "22° / 26°"));
        dailyData.add(new DailyData("31 thg 10 Thứ 6", R.drawable.ic_rain, "22° / 26°"));
        dailyData.add(new DailyData("1 thg 11 Thứ 7", R.drawable.ic_cloudy, "20° / 25°"));

        // Khởi tạo và set adapter
        dailyAdapter = new DailyForecastAdapter(this, dailyData);
        dailyForecastRecyclerView.setAdapter(dailyAdapter);
    }
}
