package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class ThermalStressEngine {

    public double calculateWBGT(double tempC, double humidityPct) {
        double e = (humidityPct / 100.0) * 6.105
                * Math.exp((17.27 * tempC) / (237.7 + tempC));
        return 0.567 * tempC + 0.393 * e + 3.94;
    }

    public double calculateUTCIApprox(double tempC, double humidityPct,
                                      double windSpeedKmh, double solarRadWm2) {
        double windMs = windSpeedKmh / 3.6;
        double radAdjustment = solarRadWm2 / 800.0 * 2.5;
        double windCooling = Math.min(windMs * 0.7, 4.0);
        return tempC + radAdjustment - windCooling + (humidityPct - 50) * 0.05;
    }

    public String classifyRisk(double wbgt) {
        if (wbgt < 28) return "safe";
        if (wbgt < 30) return "caution";
        if (wbgt < 32) return "danger";
        return "extreme_danger";
    }

    public ThermalResult evaluate(WeatherReading reading) {
        double wbgt = calculateWBGT(reading.tempC(), reading.humidityPct());
        double utci = calculateUTCIApprox(reading.tempC(), reading.humidityPct(),
                reading.windSpeedKmh(), reading.solarRadiationWm2());
        String tier = classifyRisk(wbgt);
        return new ThermalResult(wbgt, utci, tier);
    }
}