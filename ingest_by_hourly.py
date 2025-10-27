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



def wind_direction_to_cardinal(degree):
    dirs = [
        "Bắc", "Đông Bắc", "Đông", "Đông Nam",
        "Nam", "Tây Nam", "Tây", "Tây Bắc"
    ]
    ix = int(((degree + 22.5) % 360) // 45)
    return dirs[ix]

def get_coordinates(city_name):
    url = f"https://geocoding-api.open-meteo.com/v1/search?name={city_name}&count=1&language=vi"
    resp = requests.get(url).json()
    if "results" not in resp or len(resp["results"]) == 0:
        raise ValueError("Không tìm thấy thành phố")
    city = resp["results"][0]
    return city["latitude"], city["longitude"], city["name"], city.get("country", "")

def get_weather_json(city_name):
    
    latitude, longitude, name, country = get_coordinates(city_name)
    today = datetime.now().strftime("%Y-%m-%d")
    url = "https://api.open-meteo.com/v1/forecast"
    params = {
        "latitude": latitude,
        "longitude": longitude,
        "hourly": (
            "temperature_2m,weather_code,cloud_cover,uv_index,relative_humidity_2m,"
            "wind_direction_10m,wind_speed_10m,pressure_msl,visibility,rain"
        ),
        "timezone": "auto",
        "forecast_days": 1
    }
    data = requests.get(url, params=params).json()
    times = data["hourly"]["time"]
    rain_values = data["hourly"].get("rain") or data["hourly"].get("precipitation") or [None]*len(times)
    output = []
    for i, time in enumerate(times):
        if time.startswith(today):
            code = data['hourly']['weather_code'][i]
            description = weather_map.get(code, "Không xác định")
            wind_deg = data['hourly']['wind_direction_10m'][i]
            wind_cardinal = wind_direction_to_cardinal(wind_deg)
            out_item = {
                "time": time,
                "temperature": round(data['hourly']['temperature_2m'][i]),
                "weather": description,
                "cloud_cover": data['hourly']['cloud_cover'][i],
                "uv_index": data['hourly']['uv_index'][i],
                "humidity": data['hourly']['relative_humidity_2m'][i],
                "wind_speed": data['hourly']['wind_speed_10m'][i],
                "wind_direction": wind_cardinal,
                "wind_degree": wind_deg,
                "pressure": data['hourly']['pressure_msl'][i],
                "visibility": data['hourly']['visibility'][i],
                "rain": rain_values[i],
            }
            output.append(out_item)
    return output