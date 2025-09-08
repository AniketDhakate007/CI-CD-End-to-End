package com.weatherApp.CI.CD.demo.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {

    @Value("${weather.api_key}")@Autowired
    private String apiKey;

    @Autowired
    private RestTemplate rest;
    @Autowired
    private ObjectMapper mapper;

    public Map<String, Object> getWeather(String city) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenWeatherMap API key not configured (OWM_API_KEY env var)");
        }

        String q = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = String.format(" http://api.weatherapi.com/v1/data/2.5/weather?q=%s&units=metric&appid=%s", q, apiKey);
        String resp = rest.getForObject(url, String.class);
        JsonNode root = mapper.readTree(resp);

        Map<String, Object> out = new HashMap<>();
        out.put("city", root.path("name").asText());
        out.put("country", root.path("sys").path("country").asText());
        out.put("temp", root.path("main").path("temp").asDouble());
        out.put("feels_like", root.path("main").path("feels_like").asDouble());
        out.put("humidity", root.path("main").path("humidity").asInt());
        if (root.path("weather").isArray() && root.path("weather").size() > 0) {
            out.put("description", root.path("weather").get(0).path("description").asText());
        }
        return out;
    }
}
