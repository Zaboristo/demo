package com.meteo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenMeteoClient {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String LATITUDE = "52.52";
    private static final String LONGITUDE = "13.41";
    private static final String PARAMETERS = "current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    public String getWeatherForecast() {
        try {
            // Build the complete URL for the API request
            String urlString = BASE_URL + "?latitude=" + LATITUDE + "&longitude=" + LONGITUDE + "&" + PARAMETERS;
            URL url = new URL(urlString);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Check if the request was successful
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + responseCode);
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Return the response as a String
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        OpenMeteoClient api = new OpenMeteoClient();
        String weatherForecast = api.getWeatherForecast();
        System.out.println(weatherForecast);
    }
}