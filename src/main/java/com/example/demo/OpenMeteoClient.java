package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class OpenMeteoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherReading fetchCurrentWeather(double lat, double lon) {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,shortwave_radiation";

        Map response = restTemplate.getForObject(url, Map.class);
        Map current = (Map) response.get("current");

        double temp = ((Number) current.get("temperature_2m")).doubleValue();
        double humidity = ((Number) current.get("relative_humidity_2m")).doubleValue();
        double wind = ((Number) current.get("wind_speed_10m")).doubleValue();
        double solar = ((Number) current.get("shortwave_radiation")).doubleValue();

        return new WeatherReading(temp, humidity, wind, solar);
    }

    public Map fetchDailyForecast(double lat, double lon) {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=temperature_2m_max,relative_humidity_2m_mean"
                + "&forecast_days=6";

        Map response = restTemplate.getForObject(url, Map.class);
        return (Map) response.get("daily");
    }
}