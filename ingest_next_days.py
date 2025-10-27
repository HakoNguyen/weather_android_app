import requests
from datetime import datetime

weather_map = {
    0: "Trời quang, nắng ☀️",
    1: "Trời gần như quang mây 🌤️",
    2: "Có mây, trời dễ chịu ⛅",
    3: "Trời nhiều mây ☁️",
    45: "Sương mù 🌫️",
    48: "Sương muối",
    51: "Mưa phùn nhẹ",
    53: "Mưa phùn vừa",
    55: "Mưa phùn dày",
    56: "Mưa phùn lạnh nhẹ",
    57: "Mưa phùn lạnh dày",
    61: "Mưa nhỏ 🌦️",
    63: "Mưa vừa 🌧️",
    65: "Mưa to 🌧️",
    66: "Mưa đóng băng nhẹ",
    67: "Mưa đóng băng to",
    71: "Tuyết rơi nhẹ ❄️",
    73: "Tuyết rơi vừa ❄️",
    75: "Tuyết rơi to ❄️",
    77: "Bông tuyết",
    80: "Mưa rào nhẹ",
    81: "Mưa rào vừa",
    82: "Mưa rào to",
    85: "Tuyết rào nhẹ",
    86: "Tuyết rào to",
    95: "Dông ⚡",
    96: "Dông kèm mưa đá nhẹ",
    99: "Dông kèm mưa đá to"
}

def get_coordinates(city_name):
    url = f"https://geocoding-api.open-meteo.com/v1/search?name={city_name}&count=1&language=vi"
    resp = requests.get(url).json()
    if "results" not in resp or len(resp["results"]) == 0:
        raise ValueError("Không tìm thấy thành phố")
    city = resp["results"][0]
    return city["latitude"], city["longitude"], city["name"], city.get("country", "")


def get_weather_next_days(city_name):
    latitude, longitude, name, country = get_coordinates(city_name)
    url = "https://api.open-meteo.com/v1/forecast"
    params = {
        "latitude": latitude,
        "longitude": longitude,
        "daily": "temperature_2m_max,temperature_2m_min,weather_code",
        "timezone": "auto",
        "forecast_days": 7
    }
    data = requests.get(url, params=params).json()
    days = data["daily"]["time"]
    temps_max = data["daily"]["temperature_2m_max"]
    temps_min = data["daily"]["temperature_2m_min"]
    codes = data["daily"]["weather_code"]
    out = []
    for i, day in enumerate(days):
        state = weather_map.get(codes[i], "Không xác định")
        out.append({
            "date": day,
            "Nhiệt độ cao nhất": round(temps_max[i]),
            "Nhiệt độ thấp nhất": round(temps_min[i]),
            "Trạng thái thời tiết": state
        })
    return out