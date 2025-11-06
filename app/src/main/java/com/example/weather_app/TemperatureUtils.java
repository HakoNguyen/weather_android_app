package com.example.weather_app;

public class TemperatureUtils {
    
    /**
     * Chuyển đổi nhiệt độ từ Celsius sang Fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }
    
    /**
     * Chuyển đổi nhiệt độ từ Fahrenheit sang Celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }
    
    /**
     * Format nhiệt độ với đơn vị
     */
    public static String formatTemperature(double temperature, boolean isFahrenheit) {
        if (isFahrenheit) {
            double fahrenheit = celsiusToFahrenheit(temperature);
            return String.format("%.0f°F", fahrenheit);
        } else {
            return String.format("%.0f°C", temperature);
        }
    }
    
    /**
     * Format nhiệt độ với đơn vị (1 số thập phân)
     */
    public static String formatTemperatureWithDecimal(double temperature, boolean isFahrenheit) {
        if (isFahrenheit) {
            double fahrenheit = celsiusToFahrenheit(temperature);
            return String.format("%.1f°F", fahrenheit);
        } else {
            return String.format("%.1f°C", temperature);
        }
    }
    
    /**
     * Lấy giá trị nhiệt độ đã convert (trả về double để dùng cho biểu đồ)
     */
    public static double getConvertedTemperature(double celsius, boolean isFahrenheit) {
        if (isFahrenheit) {
            return celsiusToFahrenheit(celsius);
        }
        return celsius;
    }
}

