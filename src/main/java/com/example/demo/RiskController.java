package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class RiskController {

    @Autowired
    private OpenMeteoClient openMeteoClient;

    @Autowired
    private ThermalStressEngine thermalStressEngine;

    @Autowired
    private GeocodingClient geocodingClient;

    @GetMapping("/api/v1/risk")
    public ThermalResult getRiskByCoordinates(
            @RequestParam double lat,
            @RequestParam double lon) {
        WeatherReading reading = openMeteoClient.fetchCurrentWeather(lat, lon);
        return thermalStressEngine.evaluate(reading);
    }

    @GetMapping("/api/v1/risk/city")
    public ThermalResult getRiskByCityName(@RequestParam String name) {
        double[] coords = geocodingClient.lookupCoordinates(name);
        WeatherReading reading = openMeteoClient.fetchCurrentWeather(coords[0], coords[1]);
        return thermalStressEngine.evaluate(reading);
    }

    @GetMapping("/api/v1/risk/cities")
    public List<CityRisk> getRiskForMajorCities() {
        String[] cities = { "Mumbai", "Delhi", "Kolkata", "Bengaluru" };
        List<CityRisk> results = new ArrayList<>();

        for (String city : cities) {
            double[] coords = geocodingClient.lookupCoordinates(city);
            WeatherReading reading = openMeteoClient.fetchCurrentWeather(coords[0], coords[1]);
            ThermalResult result = thermalStressEngine.evaluate(reading);
            results.add(new CityRisk(city, result.wbgt(), result.utci(), result.tier()));
        }

        return results;
    }

    @GetMapping("/api/v1/risk/forecast")
    public List<DailyForecast> getForecast(@RequestParam String name) {
        double[] coords = geocodingClient.lookupCoordinates(name);
        Map daily = openMeteoClient.fetchDailyForecast(coords[0], coords[1]);

        List<String> dates = (List<String>) daily.get("time");
        List<Number> maxTemps = (List<Number>) daily.get("temperature_2m_max");
        List<Number> humidities = (List<Number>) daily.get("relative_humidity_2m_mean");

        List<DailyForecast> forecast = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            double temp = maxTemps.get(i).doubleValue();
            double humidity = humidities.get(i).doubleValue();
            double wbgt = thermalStressEngine.calculateWBGT(temp, humidity);
            String tier = thermalStressEngine.classifyRisk(wbgt);
            forecast.add(new DailyForecast(dates.get(i), temp, wbgt, tier));
        }

        return forecast;
    }
}