package com.meteo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenMeteoClient {

    private static final String API_URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m&current_weather=true";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getWeatherForecast() throws IOException {
        String urlString = String.format(API_URL_TEMPLATE);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = in.lines().collect(Collectors.joining());
            return parseWeatherForecast(response);
        }
    }

    private String parseWeatherForecast(String jsonResponse) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Parsing current weather
        JsonNode currentWeatherNode = rootNode.path("current");
        CurrentWeather currentWeather = new CurrentWeather(
                currentWeatherNode.path("time").asText(),
                currentWeatherNode.path("interval").asInt(),
                currentWeatherNode.path("temperature_2m").asDouble(),
                currentWeatherNode.path("wind_speed_10m").asDouble()
        );

        // Parsing hourly weather data
        JsonNode hourlyNode = rootNode.path("hourly");
        List<String> times = objectMapper.convertValue(hourlyNode.path("time"), List.class);
        List<Double> temperatures = objectMapper.convertValue(hourlyNode.path("temperature_2m"), List.class);
        List<Integer> humidityLevels = objectMapper.convertValue(hourlyNode.path("relative_humidity_2m"), List.class);
        List<Double> windSpeeds = objectMapper.convertValue(hourlyNode.path("wind_speed_10m"), List.class);

        HourlyWeather hourlyWeather = new HourlyWeather(times, temperatures, humidityLevels, windSpeeds);

        return new WeatherForecast(currentWeather, hourlyWeather).toString();
    }
}