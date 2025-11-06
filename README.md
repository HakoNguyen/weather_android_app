# 🌤️ Weather App - Ứng Dụng Thời Tiết Android

## 📋 Tổng Quan

**Weather App** là một ứng dụng Android hiện đại để xem thời tiết với nhiều tính năng nâng cao. Ứng dụng cung cấp thông tin thời tiết chi tiết theo giờ và theo ngày, hỗ trợ nhiều thành phố, tích hợp bản đồ, và nhiều tính năng tiện ích khác.

### 🎯 Mục Tiêu Dự Án
- Cung cấp thông tin thời tiết chính xác và chi tiết
- Tích hợp các tính năng nâng cao như GPS, bản đồ, widget
- Giao diện đẹp mắt, dễ sử dụng
- Phù hợp cho bài kiểm tra giữa kỳ với đầy đủ tính năng

---

## ✨ Tính Năng Chính

### 1. 📍 **Location Services (GPS)**
Tự động lấy vị trí hiện tại của người dùng để hiển thị thời tiết.

**Công nghệ sử dụng:**
- **Google Play Services Location API** (`com.google.android.gms:play-services-location:21.0.1`)
- **FusedLocationProviderClient**: Lấy vị trí GPS với độ chính xác cao
- **Geocoder**: Chuyển đổi tọa độ (latitude/longitude) thành tên thành phố

**Logic hoạt động:**
```java
// LocationHelper.java
1. Kiểm tra quyền truy cập vị trí (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
2. Sử dụng FusedLocationProviderClient.getCurrentLocation() với Priority.PRIORITY_HIGH_ACCURACY
3. Khi có Location, sử dụng Geocoder để reverse geocoding:
   - getFromLocation(latitude, longitude, 1) → Address
   - Lấy city từ address.getLocality() hoặc address.getAdminArea()
4. Trả về tên thành phố qua LocationCallback
```

**Quyền cần thiết:**
- `ACCESS_FINE_LOCATION`: Vị trí chính xác (GPS)
- `ACCESS_COARSE_LOCATION`: Vị trí gần đúng (mạng)

**Luồng hoạt động:**
1. User click icon 📍 trên toolbar
2. App kiểm tra quyền → Nếu chưa có → Request permission
3. Lấy vị trí GPS → Geocoding → Lấy tên thành phố
4. Fetch weather data cho thành phố đó

---

### 2. 💾 **SharedPreferences - Lưu Trữ Dữ Liệu**

Lưu trữ các thông tin cài đặt và dữ liệu người dùng.

**Dữ liệu được lưu:**
- **Last City**: Thành phố đã tìm kiếm cuối cùng
- **Favorite Cities**: Danh sách thành phố yêu thích (Set<String>)
- **GPS Preference**: Có sử dụng GPS tự động hay không
- **Temperature Unit**: Đơn vị nhiệt độ (°C hoặc °F)
- **Widget Data**: Dữ liệu cho home screen widget

**Logic lưu trữ:**
```java
// PreferencesHelper.java
- Sử dụng SharedPreferences với tên "WeatherAppPrefs"
- Lưu String: sharedPreferences.edit().putString(key, value).apply()
- Lưu Set<String>: sharedPreferences.edit().putStringSet(key, set).apply()
- Lưu Boolean: sharedPreferences.edit().putBoolean(key, value).apply()
```

**Khi nào được lưu:**
- Khi user tìm kiếm thành phố → `saveLastCity()`
- Khi thêm/xóa favorite → `addFavoriteCity()` / `removeFavoriteCity()`
- Khi thay đổi setting → `setUseGps()`, `setTemperatureUnit()`
- Khi cập nhật widget → `saveWidgetData()`

---

### 3. ⏳ **Loading States**

Hiển thị trạng thái đang tải dữ liệu.

**Công nghệ:**
- **ProgressBar**: Hiển thị khi đang fetch API
- **SwipeRefreshLayout**: Pull-to-refresh với loading indicator

**Logic:**
```java
// MainActivity.java
private int pendingApiCalls = 0;

// Khi bắt đầu fetch
pendingApiCalls = 2; // hourly + daily
showLoading(true);

// Khi mỗi API call hoàn thành
checkAllApiCallsComplete() {
    pendingApiCalls--;
    if (pendingApiCalls <= 0) {
        showLoading(false); // Ẩn loading khi tất cả hoàn thành
    }
}
```

**Cải thiện UX:**
- User biết app đang xử lý
- Tránh nhiều lần click khi đang tải
- Loading tự động ẩn khi hoàn tất

---

### 4. ⭐ **Favorite Cities (Thành Phố Yêu Thích)**

Quản lý danh sách thành phố yêu thích để truy cập nhanh.

**Tính năng:**
- Thêm/xóa thành phố yêu thích
- Xem danh sách tất cả thành phố yêu thích
- Click vào thành phố để xem thời tiết ngay lập tức
- Tìm kiếm trong danh sách yêu thích

**Activities:**
- **FavoriteCitiesActivity**: Màn hình quản lý danh sách
  - SearchView để filter
  - RecyclerView hiển thị danh sách
  - Delete button cho mỗi thành phố
  - Click để chọn thành phố

**Adapter:**
- **FavoriteCitiesAdapter**: Adapter cho RecyclerView
- Hỗ trợ filtering với SearchView

**Luồng hoạt động:**
1. User vào Settings → "Quản lý thành phố yêu thích"
2. Mở FavoriteCitiesActivity → Hiển thị danh sách
3. User có thể:
   - Search để filter
   - Click thành phố → Trả về MainActivity với thành phố đó
   - Delete để xóa khỏi yêu thích

---

### 5. 🔄 **Pull to Refresh**

Làm mới dữ liệu thời tiết bằng cách kéo xuống.

**Công nghệ:**
- **SwipeRefreshLayout**: Material Design component

**Logic:**
```java
// MainActivity.java
binding.swipeRefreshLayout.setOnRefreshListener(() -> {
    fetchWeatherData(currentCity); // Refresh data
});
```

**Hoạt động:**
- User kéo màn hình xuống
- Hiển thị loading indicator
- Fetch lại data từ API
- Tự động ẩn indicator khi hoàn tất

---

### 6. ⚙️ **Settings Activity**

Màn hình cài đặt với nhiều tùy chọn.

**Tính năng:**
- **Auto Location Toggle**: Bật/tắt tự động lấy vị trí GPS
- **Add/Remove Favorite**: Thêm/xóa thành phố hiện tại vào yêu thích
- **Manage Favorite Cities**: Mở màn hình quản lý yêu thích
- **Temperature Unit Toggle**: Chuyển đổi °C/°F

**Activity:**
- **SettingsActivity**: Màn hình settings với CardView layout

**Logic:**
- SwitchCompat cho toggle options
- Intent để mở FavoriteCitiesActivity
- SharedPreferences để lưu settings

---

### 7. 📊 **Temperature Chart (Biểu Đồ Nhiệt Độ)**

Hiển thị biểu đồ đường nhiệt độ theo giờ.

**Công nghệ:**
- **MPAndroidChart** (`com.github.PhilJay:MPAndroidChart:3.1.0`)
- **LineChart**: Biểu đồ đường

**Logic:**
```java
// MainActivity.java
private void updateTemperatureChart(List<HourlyResponse> hourlyList) {
    List<Entry> entries = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    
    // Lấy 24 giờ đầu tiên
    for (int i = 0; i < Math.min(24, hourlyList.size()); i++) {
        HourlyResponse hour = hourlyList.get(i);
        double temp = TemperatureUtils.getConvertedTemperature(
            hour.getTemperature(), isFahrenheit);
        entries.add(new Entry(i, (float) temp));
        
        // Format time từ "2025-11-06T14:00" → "14:00"
        labels.add(formatTimeLabel(hour.getTime()));
    }
    
    LineDataSet dataSet = new LineDataSet(entries, "Nhiệt độ");
    // Cấu hình màu sắc, style
    LineData lineData = new LineData(dataSet);
    chart.setData(lineData);
    chart.invalidate();
}
```

**Tính năng:**
- Hiển thị 24 giờ đầu tiên
- Convert nhiệt độ theo đơn vị (°C/°F)
- Màu sắc đẹp mắt, có gradient
- Zoom và scroll

---

### 8. 🗺️ **Map Activity (Bản Đồ)**

Chọn vị trí trên bản đồ để xem thời tiết.

**Công nghệ:**
- **OSMDroid** (`org.osmdroid:osmdroid-android:6.1.18`)
  - **100% FREE**, không cần API key
  - Sử dụng OpenStreetMap tiles

**Tính năng:**
- Hiển thị bản đồ với vị trí hiện tại (thành phố đang xem)
- Click trên bản đồ để chọn vị trí mới
- Geocoding để lấy tên thành phố từ tọa độ
- Search bar để tìm thành phố
- Nút "Vị trí hiện tại" để lấy GPS location
- Zoom controls (min: 3.0, max: 21.0)

**MapActivity.java Logic:**
```java
// 1. Khởi tạo MapView với TileSourceFactory.MAPNIK
mapView.setTileSource(TileSourceFactory.MAPNIK);
mapView.setMultiTouchControls(true);
mapView.setBuiltInZoomControls(true);

// 2. Hiển thị vị trí hiện tại (từ currentCity)
locationHelper.getCoordinatesFromCityName(currentCity, (lat, lng) -> {
    // Đặt marker và di chuyển map đến vị trí
    addMarker(lat, lng);
    mapController.setCenter(new GeoPoint(lat, lng));
});

// 3. Xử lý click trên bản đồ
MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        // Lấy tên thành phố từ tọa độ
        locationHelper.getCityNameFromCoordinates(p.getLatitude(), p.getLongitude(), cityName -> {
            // Đặt marker mới
            // Có thể confirm để trả về MainActivity
        });
        return true;
    }
});

// 4. Search thành phố
searchView.setOnQueryTextListener(query -> {
    locationHelper.getCoordinatesFromCityName(query, (lat, lng) -> {
        // Di chuyển map đến thành phố tìm được
    });
});
```

**Luồng hoạt động:**
1. User click icon bản đồ trên MainActivity
2. Mở MapActivity với `currentCity` (thành phố đang xem)
3. Map hiển thị vị trí của thành phố đó
4. User có thể:
   - Click trên map để chọn vị trí mới
   - Search thành phố
   - Click nút "Vị trí hiện tại" để lấy GPS
5. Click "Xác nhận" → Trả về MainActivity với thành phố mới

---

### 9. 🔔 **Weather Alerts (Cảnh Báo Thời Tiết)**

Thông báo cảnh báo khi có điều kiện thời tiết khắc nghiệt.

**Công nghệ:**
- **NotificationManager**: Android Notification System
- **NotificationChannel**: Channel cho Android 8.0+

**Điều kiện cảnh báo:**
- Nhiệt độ quá cao: > 35°C
- Nhiệt độ quá thấp: < 5°C
- Mưa lớn: > 10mm
- Gió mạnh: > 15 m/s

**Logic:**
```java
// MainActivity.java
private void checkWeatherAlerts(HourlyResponse currentHour) {
    double temp = currentHour.getTemperature();
    double rain = currentHour.getRain();
    double windSpeed = currentHour.getWindSpeed();
    
    if (temp > 35) {
        WeatherAlertHelper.showAlert(this, 
            "Nhiệt độ cao", 
            "Nhiệt độ hiện tại: " + temp + "°C. Hãy cẩn thận!");
    }
    if (temp < 5) {
        WeatherAlertHelper.showAlert(this, 
            "Nhiệt độ thấp", 
            "Nhiệt độ hiện tại: " + temp + "°C. Hãy mặc ấm!");
    }
    if (rain > 10) {
        WeatherAlertHelper.showAlert(this, 
            "Mưa lớn", 
            "Lượng mưa: " + rain + "mm. Hãy mang theo ô!");
    }
    if (windSpeed > 15) {
        WeatherAlertHelper.showAlert(this, 
            "Gió mạnh", 
            "Tốc độ gió: " + windSpeed + " m/s. Hãy cẩn thận!");
    }
}
```

**WeatherAlertHelper.java:**
```java
// Tạo notification channel (Android 8.0+)
public static void createNotificationChannel(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }
}

// Hiển thị notification
public static void showAlert(Context context, String title, String message) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true);
    
    notificationManager.notify(NOTIFICATION_ID, builder.build());
}
```

**Quyền:**
- `POST_NOTIFICATIONS` (Android 13+)

---

### 10. 📤 **Share Weather (Chia Sẻ Thời Tiết)**

Chia sẻ thông tin thời tiết qua SMS/Email/Social.

**Logic:**
```java
// MainActivity.java
private void shareWeather() {
    String shareText = String.format(
        "Thời tiết tại %s: %s, %s. Nhiệt độ: %s - %s",
        currentCity,
        currentConditionText,
        TemperatureUtils.formatTemperature(currentHour.getTemperature(), isFahrenheit),
        TemperatureUtils.formatTemperature(currentDayData.getTempMin(), isFahrenheit),
        TemperatureUtils.formatTemperature(currentDayData.getTempMax(), isFahrenheit)
    );
    
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
    startActivity(Intent.createChooser(shareIntent, "Chia sẻ thời tiết"));
}
```

**Format:**
```
Thời tiết tại Hà Nội: Trời quang, 25°C. Nhiệt độ: 22°C - 28°C
```

---

### 11. 🌡️ **Temperature Unit Conversion (Chuyển Đổi Đơn vị)**

Chuyển đổi giữa Celsius (°C) và Fahrenheit (°F).

**Công nghệ:**
- **TemperatureUtils**: Utility class cho conversion

**Công thức:**
```java
// TemperatureUtils.java
Celsius → Fahrenheit: F = (C × 9/5) + 32
Fahrenheit → Celsius: C = (F - 32) × 5/9
```

**Logic:**
```java
// Lưu preference
preferencesHelper.setTemperatureUnit(isFahrenheit ? "F" : "C");

// Áp dụng toàn app
boolean isFahrenheit = preferencesHelper.isFahrenheit();

// Format nhiệt độ
String formatted = TemperatureUtils.formatTemperature(temp, isFahrenheit);
// → "25°C" hoặc "77°F"
```

**Nơi áp dụng:**
- MainActivity: Nhiệt độ hiện tại, min/max
- HourlyWeatherAdapter: Nhiệt độ theo giờ
- DailyWeatherAdapter: Nhiệt độ theo ngày
- Temperature Chart: Trục Y của biểu đồ
- Share Weather: Text chia sẻ

---

### 12. 🏠 **Home Screen Widget**

Widget hiển thị thời tiết trên màn hình chính.

**Công nghệ:**
- **AppWidgetProvider**: Android Widget System
- **RemoteViews**: Layout cho widget

**WeatherWidgetProvider.java:**
```java
public class WeatherWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Lấy dữ liệu từ SharedPreferences
        PreferencesHelper prefs = new PreferencesHelper(context);
        String temp = sharedPreferences.getString("widget_temp", "--°");
        String condition = sharedPreferences.getString("widget_condition", "Không có dữ liệu");
        
        // Tạo RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        views.setTextViewText(R.id.widget_temp, temp);
        views.setTextViewText(R.id.widget_condition, condition);
        
        // Set click listener để mở MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(...);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        
        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
```

**Cập nhật widget:**
```java
// MainActivity.java
private void updateWidget() {
    preferencesHelper.saveWidgetData(
        TemperatureUtils.formatTemperature(currentHour.getTemperature(), isFahrenheit),
        currentConditionText,
        currentHour.getWeather(),
        TemperatureUtils.formatTemperature(currentDayData.getTempMin(), isFahrenheit) + 
        " / " + 
        TemperatureUtils.formatTemperature(currentDayData.getTempMax(), isFahrenheit)
    );
    
    // Trigger widget update
    Intent intent = new Intent(this, WeatherWidgetProvider.class);
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(
        new ComponentName(this, WeatherWidgetProvider.class));
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    sendBroadcast(intent);
}
```

**Cấu hình:**
- `widget_weather_info.xml`: Cấu hình widget (kích thước, update period)
- `widget_weather.xml`: Layout của widget

**Update Period:**
- `updatePeriodMillis="1800000"` (30 phút)

---

### 13. 🎨 **Animated Weather Icons**

Animation cho icon thời tiết.

**Animations:**
- **weather_icon_fade_in.xml**: Fade in + scale
- **sun_rotate.xml**: Xoay mặt trời (infinite)
- **cloud_float.xml**: Float animation cho mây

**Logic:**
```java
// MainActivity.java
private void updateWeatherIconWithAnimation(String weather) {
    int iconRes = getWeatherIconResource(weather);
    binding.includeCurrent.ivCurrentWeatherIcon.setImageResource(iconRes);
    
    Animation animation = null;
    if (weather.contains("nắng") || weather.contains("quang")) {
        animation = AnimationUtils.loadAnimation(this, R.anim.sun_rotate);
    } else if (weather.contains("mây")) {
        animation = AnimationUtils.loadAnimation(this, R.anim.cloud_float);
    } else {
        animation = AnimationUtils.loadAnimation(this, R.anim.weather_icon_fade_in);
    }
    
    binding.includeCurrent.ivCurrentWeatherIcon.startAnimation(animation);
}
```

---

## 🔧 Công Nghệ Sử Dụng

### **Core Android**
- **Android SDK**: compileSdk 36, minSdk 24, targetSdk 36
- **Java**: Java 11
- **ViewBinding**: Ràng buộc view tự động
- **DataBinding**: Binding dữ liệu

### **UI Components**
- **Material Design**: Material Components
- **RecyclerView**: Hiển thị danh sách
- **CardView**: Card layout
- **SwipeRefreshLayout**: Pull-to-refresh
- **SearchView**: Tìm kiếm
- **ProgressBar**: Loading indicator

### **Networking**
- **Retrofit 2.9.0**: REST API client
- **GSON Converter 2.9.0**: JSON parsing
- **GSON 2.10.1**: JSON library

### **Location Services**
- **Google Play Services Location 21.0.1**: GPS location
- **FusedLocationProviderClient**: High accuracy location
- **Geocoder**: Reverse/Forward geocoding

### **Maps**
- **OSMDroid 6.1.18**: OpenStreetMap library (FREE)

### **Charting**
- **MPAndroidChart 3.1.0**: Temperature line chart

### **Notifications**
- **NotificationManager**: System notifications
- **NotificationChannel**: Android 8.0+ channels

### **Widgets**
- **AppWidgetProvider**: Home screen widgets
- **RemoteViews**: Widget layout

### **Data Persistence**
- **SharedPreferences**: Lưu trữ settings và data

---

## 📁 Cấu Trúc Project

```
app/src/main/
├── java/com/example/weather_app/
│   ├── MainActivity.java              # Activity chính
│   ├── SettingsActivity.java          # Màn hình cài đặt
│   ├── FavoriteCitiesActivity.java    # Quản lý thành phố yêu thích
│   ├── MapActivity.java               # Bản đồ chọn vị trí
│   ├── WeatherService.java            # Retrofit API interface
│   ├── RetrofitClient.java            # Retrofit client setup
│   ├── HourlyResponse.java            # Model: Dữ liệu theo giờ
│   ├── DailyResponse.java             # Model: Dữ liệu theo ngày
│   ├── HourlyWeatherAdapter.java      # Adapter cho hourly RecyclerView
│   ├── DailyWeatherAdapter.java       # Adapter cho daily RecyclerView
│   ├── FavoriteCitiesAdapter.java     # Adapter cho favorite cities
│   ├── LocationHelper.java            # Helper: Location services
│   ├── PreferencesHelper.java         # Helper: SharedPreferences
│   ├── TemperatureUtils.java          # Helper: Temperature conversion
│   ├── WeatherAlertHelper.java        # Helper: Notifications
│   └── WeatherWidgetProvider.java     # Widget provider
│
├── res/
│   ├── layout/                        # XML layouts
│   │   ├── activity_main.xml
│   │   ├── activity_settings.xml
│   │   ├── activity_favorite_cities.xml
│   │   ├── activity_map.xml
│   │   ├── card_current_weather.xml
│   │   ├── card_hourly_forecast.xml
│   │   ├── card_daily_forecast.xml
│   │   ├── card_temperature_chart.xml
│   │   ├── card_weather_details.xml
│   │   ├── card_sun_moon.xml
│   │   ├── widget_weather.xml
│   │   └── item_*.xml                 # Item layouts cho RecyclerView
│   │
│   ├── drawable/                      # Icons, shapes, animations
│   │   ├── ic_*.xml                   # Vector icons
│   │   ├── weather_icon_fade_in.xml
│   │   ├── sun_rotate.xml
│   │   └── cloud_float.xml
│   │
│   ├── values/                        # Resources
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   └── themes.xml
│   │
│   └── xml/                           # Configuration
│       ├── widget_weather_info.xml
│       └── network_security_config.xml
│
└── AndroidManifest.xml                 # App configuration
```

---

## 🔄 Luồng Hoạt Động

### **1. Luồng Khởi Động App**

```
onCreate()
  ↓
Initialize: weatherService, preferencesHelper, locationHelper
  ↓
Setup UI: RecyclerView, SearchView, Buttons, SwipeRefresh
  ↓
Check Settings:
  ├─ If (shouldUseGps() && hasLocationPermission())
  │   └─ getLocationAndFetchWeather()
  └─ Else
      └─ fetchWeatherData(getLastCity())
```

### **2. Luồng Tìm Kiếm Thành Phố**

```
User nhập tên thành phố trong SearchView
  ↓
onQueryTextSubmit(cityName)
  ↓
saveLastCity(cityName) → SharedPreferences
  ↓
fetchWeatherData(cityName)
  ↓
  ├─ fetchHourlyWeather(cityName) → API call
  │   └─ onResponse() → Update UI hourly
  │
  └─ fetchDailyWeather(cityName) → API call
      └─ onResponse() → Update UI daily
  ↓
checkAllApiCallsComplete()
  ↓
  ├─ updateTemperatureChart()
  ├─ scrollToHourlyPosition() (scroll to current time)
  ├─ checkWeatherAlerts()
  └─ updateWidget()
```

### **3. Luồng Lấy Vị Trí GPS**

```
User click icon 📍
  ↓
setupLocationButton()
  ↓
Check permission:
  ├─ If (hasLocationPermission())
  │   └─ getLocationAndFetchWeather()
  └─ Else
      └─ requestPermissions() → onRequestPermissionsResult()
          └─ If granted → getLocationAndFetchWeather()
  ↓
getLocationAndFetchWeather()
  ↓
locationHelper.getCurrentCity(callback)
  ↓
  ├─ FusedLocationProviderClient.getCurrentLocation()
  │   └─ onSuccess: Location (lat, lng)
  │       └─ Geocoder.getFromLocation(lat, lng, 1)
  │           └─ Address → cityName
  │               └─ callback.onLocationSuccess(cityName)
  │                   └─ fetchWeatherData(cityName)
  │
  └─ onFailure: callback.onLocationFailure(error)
```

### **4. Luồng Chọn Vị Trí Trên Bản Đồ**

```
User click icon 🗺️
  ↓
startActivityForResult(MapActivity, REQUEST_CODE_MAP)
  ↓
MapActivity.onCreate()
  ↓
showCurrentCityLocation() (hiển thị vị trí hiện tại)
  ↓
User click trên map hoặc search
  ↓
locationHelper.getCityNameFromCoordinates() hoặc getCoordinatesFromCityName()
  ↓
User click "Xác nhận"
  ↓
setResult(RESULT_OK, intent với cityName)
  ↓
finish()
  ↓
MainActivity.onActivityResult()
  ↓
fetchWeatherData(selectedCity)
```

### **5. Luồng Quản Lý Thành Phố Yêu Thích**

```
User vào Settings → "Quản lý thành phố yêu thích"
  ↓
startActivityForResult(FavoriteCitiesActivity, REQUEST_CODE_FAVORITES)
  ↓
FavoriteCitiesActivity.onCreate()
  ↓
Load favorite cities từ PreferencesHelper
  ↓
Display trong RecyclerView
  ↓
User có thể:
  ├─ Search để filter
  ├─ Click thành phố → setResult(RESULT_OK, cityName) → finish()
  └─ Delete thành phố → removeFavoriteCity() → Refresh list
  ↓
MainActivity.onActivityResult()
  ↓
fetchWeatherData(selectedCity)
```

---

## 🧠 Logic Code Chi Tiết

### **1. Tìm Giờ Hiện Tại Gần Nhất**

```java
// MainActivity.java
private HourlyResponse findClosestHourlyEntry(List<HourlyResponse> hourlyList) {
    LocalDateTime now = LocalDateTime.now();
    HourlyResponse closest = null;
    long minDiff = Long.MAX_VALUE;
    
    for (HourlyResponse hour : hourlyList) {
        try {
            LocalDateTime hourTime = LocalDateTime.parse(
                hour.getTime(), 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
            
            long diff = Math.abs(ChronoUnit.MINUTES.between(now, hourTime));
            
            // Ưu tiên giờ trong tương lai hoặc gần hiện tại (trong 3 giờ)
            if (hourTime.isAfter(now) || hourTime.isAfter(now.minusHours(3))) {
                if (diff < minDiff) {
                    minDiff = diff;
                    closest = hour;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time", e);
        }
    }
    
    return closest != null ? closest : hourlyList.get(0);
}
```

**Logic:**
1. Lấy thời gian hiện tại của thiết bị (`LocalDateTime.now()`)
2. Duyệt qua tất cả hourly entries
3. Parse time từ API (format: "2025-11-06T14:00")
4. Tính khoảng cách thời gian với hiện tại
5. Ưu tiên giờ trong tương lai hoặc trong vòng 3 giờ trước
6. Trả về entry có khoảng cách nhỏ nhất

---

### **2. Tự Động Scroll Đến Giờ Hiện Tại**

```java
// MainActivity.java
private void scrollToHourlyPosition() {
    if (hourlyWeatherList.isEmpty()) return;
    
    HourlyResponse closestEntry = findClosestHourlyEntry(hourlyWeatherList);
    int position = findPositionInList(closestEntry, hourlyWeatherList);
    
    if (position >= 0) {
        // Sử dụng postDelayed để đảm bảo RecyclerView đã layout xong
        binding.includeHourly.rvHourlyForecast.postDelayed(() -> {
            binding.includeHourly.rvHourlyForecast.scrollToPosition(position);
        }, 100);
    }
}

private int findPositionInList(HourlyResponse target, List<HourlyResponse> list) {
    for (int i = 0; i < list.size(); i++) {
        if (list.get(i).getTime().equals(target.getTime())) {
            return i;
        }
    }
    return -1;
}
```

**Logic:**
1. Tìm entry gần nhất với thời gian hiện tại
2. Tìm vị trí index trong list
3. Scroll RecyclerView đến vị trí đó
4. Sử dụng `postDelayed` để đảm bảo layout đã hoàn tất

---

### **3. Logic Hiển Thị UV Index**

```java
// MainActivity.java
private void updateUVDisplay(double uvValue, String timeStr) {
    String uvText;
    
    // Kiểm tra có phải ban đêm không
    boolean isNight = isNightTime(timeStr);
    
    if (uvValue == 0) {
        if (isNight) {
            uvText = "Ban đêm";
        } else {
            uvText = "Rất thấp";
        }
    } else if (uvValue < 3) {
        uvText = "Thấp";
    } else if (uvValue < 6) {
        uvText = "Trung bình";
    } else if (uvValue < 8) {
        uvText = "Cao";
    } else if (uvValue < 11) {
        uvText = "Rất cao";
    } else {
        uvText = "Cực kỳ cao";
    }
    
    binding.includeDetails.tvUVIndex.setText(uvText);
}

private boolean isNightTime(String timeStr) {
    try {
        LocalDateTime time = LocalDateTime.parse(
            timeStr, 
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
        int hour = time.getHour();
        // Ban đêm: 18:00 - 05:59
        return hour >= 18 || hour < 6;
    } catch (Exception e) {
        return false;
    }
}
```

**Logic:**
1. Parse time từ API
2. Kiểm tra hour để xác định ban đêm (18:00-05:59)
3. Nếu UV = 0 và ban đêm → "Ban đêm"
4. Nếu UV = 0 và ban ngày → "Rất thấp"
5. Các giá trị UV khác → Phân loại theo mức độ

---

### **4. Logic Hiển Thị Giờ Thực**

```java
// MainActivity.java
private String formatCurrentRealTime() {
    LocalDateTime now = LocalDateTime.now();
    return now.format(DateTimeFormatter.ofPattern("HH:mm"));
}

// Trong updateUI
binding.includeCurrent.tvCurrentTime.setText(formatCurrentRealTime());
```

**Logic:**
- Sử dụng `LocalDateTime.now()` để lấy thời gian thực của thiết bị
- Format theo "HH:mm" (24h format)
- Hiển thị giờ thực, không phải giờ từ API

---

### **5. Logic Tracking API Calls**

```java
// MainActivity.java
private void fetchWeatherData(String cityName) {
    pendingApiCalls = 2; // hourly + daily
    showLoading(true);
    
    fetchHourlyWeather(cityName);
    fetchDailyWeather(cityName);
}

private void fetchHourlyWeather(String cityName) {
    weatherService.getHourlyWeather(cityName).enqueue(new Callback<List<HourlyResponse>>() {
        @Override
        public void onResponse(...) {
            checkAllApiCallsComplete(); // pendingApiCalls--
            // Update UI
        }
        
        @Override
        public void onFailure(...) {
            checkAllApiCallsComplete(); // pendingApiCalls--
            // Show error
        }
    });
}

private void checkAllApiCallsComplete() {
    pendingApiCalls--;
    if (pendingApiCalls <= 0) {
        showLoading(false);
        binding.swipeRefreshLayout.setRefreshing(false);
    }
}
```

**Logic:**
1. Khi bắt đầu fetch: `pendingApiCalls = 2`
2. Mỗi API call hoàn thành (success hoặc failure): `pendingApiCalls--`
3. Khi `pendingApiCalls <= 0`: Ẩn loading, cho phép tương tác

---

## 📡 API Integration

### **Weather API**

**Base URL:**
```
https://weather.sangtd.workers.dev/
```

**Endpoints:**

1. **Hourly Weather**
   ```
   GET /weather_hourly?city={cityName}
   ```
   - Response: `List<HourlyResponse>`
   - Trả về 24 giờ hoặc nhiều hơn

2. **Daily Weather**
   ```
   GET /next_days?city={cityName}
   ```
   - Response: `List<DailyResponse>`
   - Trả về các ngày tiếp theo

**Data Models:**

**HourlyResponse:**
```java
{
    "time": "2025-11-06T14:00",
    "temperature": 25,
    "weather": "Trời quang ☀️",
    "cloud_cover": 20,
    "uv_index": 5.7,
    "humidity": 75,
    "wind_speed": 3.5,
    "wind_direction": "Đông",
    "wind_degree": 90,
    "pressure": 1013.5,
    "visibility": 10000,
    "rain": 0
}
```

**DailyResponse:**
```java
{
    "date": "2025-11-06",
    "temp_min": 22,
    "temp_max": 28,
    "weather": "Trời quang",
    "sunrise": "06:00",
    "sunset": "18:00"
}
```

---

## 🔐 Permissions

### **AndroidManifest.xml**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Giải thích:**
- **INTERNET**: Kết nối API
- **ACCESS_FINE_LOCATION**: GPS chính xác
- **ACCESS_COARSE_LOCATION**: Vị trí gần đúng (mạng)
- **POST_NOTIFICATIONS**: Hiển thị notifications (Android 13+)

**Runtime Permissions:**
- Location permissions được request runtime (Android 6.0+)
- Notification permission được request runtime (Android 13+)

---

## 🎨 UI/UX Features

### **Material Design**
- Material Components
- CardView với elevation và corner radius
- Dark theme với gradient background

### **Animations**
- Weather icon animations (fade in, rotate, float)
- Smooth transitions

### **Responsive Layout**
- ScrollView để xử lý nhiều nội dung
- RecyclerView cho danh sách
- ConstraintLayout cho layout linh hoạt

---

## 📱 Activities & Fragments

### **MainActivity**
- Màn hình chính hiển thị thời tiết
- Tích hợp tất cả tính năng

### **SettingsActivity**
- Màn hình cài đặt
- Toggle switches, buttons

### **FavoriteCitiesActivity**
- Quản lý danh sách yêu thích
- Search và filter

### **MapActivity**
- Bản đồ chọn vị trí
- OSMDroid integration

---

## 🧪 Testing

### **Manual Testing Checklist**
- ✅ Tìm kiếm thành phố
- ✅ Lấy vị trí GPS
- ✅ Thêm/xóa favorite
- ✅ Pull to refresh
- ✅ Chọn vị trí trên bản đồ
- ✅ Chuyển đổi đơn vị nhiệt độ
- ✅ Chia sẻ thời tiết
- ✅ Cảnh báo thời tiết
- ✅ Home screen widget
- ✅ Settings

---

## 🚀 Build & Run

### **Requirements**
- Android Studio Arctic Fox trở lên
- JDK 11
- Android SDK 36
- Min SDK: 24 (Android 7.0)

### **Build Steps**
1. Clone repository
2. Open project in Android Studio
3. Sync Gradle
4. Build APK hoặc Run trên emulator/device

### **Gradle Commands**
```bash
./gradlew assembleDebug      # Build APK debug
./gradlew assembleRelease    # Build APK release
./gradlew installDebug       # Install trên device
```

---

