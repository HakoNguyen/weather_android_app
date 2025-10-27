from flask import Flask, request, jsonify

from ingest_by_hourly import get_weather_json
from ingest_next_days import get_weather_next_days

app = Flask(__name__)

@app.route("/weather_hourly")
def weather_api():
    city = request.args.get("city", "Hà Nội")
    result = get_weather_json(city)
    return jsonify(result)

@app.route("/next_days")
def next_days():
    city = request.args.get("city", 'Hà Nội')
    result = get_weather_next_days(city)
    return jsonify(result)

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True)

