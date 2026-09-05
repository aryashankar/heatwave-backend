package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public double[] lookupCoordinates(String cityName) {
        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + cityName
                + "&count=1&language=en&format=json";

        Map response = restTemplate.getForObject(url, Map.class);
        List<Map> results = (List<Map>) response.get("results");

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("City not found: " + cityName);
        }

        Map firstResult = results.get(0);
        double lat = ((Number) firstResult.get("latitude")).doubleValue();
        double lon = ((Number) firstResult.get("longitude")).doubleValue();

        return new double[] { lat, lon };
    }
}