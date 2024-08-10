package com.meteo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenMeteoClient {

    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m" +
            "&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    public String getWeatherForecast() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new RuntimeException("Failed to fetch weather data. HTTP error code: " + responseCode);
        }
    }
}