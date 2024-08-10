package com.meteo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenMeteoClient {

    public static void main(String[] args) throws Exception {
        System.out.println(new OpenMeteoClient().getWeatherForecast());
    }

    public OpenMeteoClient() {
    }

    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getWeatherForecast() throws IOException {
        // Create a URL object with the API URL
        URL url = new URL(API_URL);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get the response from the API
        Scanner scanner = new Scanner(connection.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        // Parse the JSON response
        JsonNode jsonNode = objectMapper.readTree(response.toString());

        // Extract the `current_units` and `current` fields
        JsonNode currentUnits = jsonNode.get("current_units");
        JsonNode current = jsonNode.get("current");

        // Remove `current_units` and `current` from the main JSON
        ((ObjectNode) jsonNode).remove("current_units");
        ((ObjectNode) jsonNode).remove("current");

        // Create a new JSON object in the desired order
        Map<String, JsonNode> orderedMap = new LinkedHashMap<>();
        jsonNode.fields().forEachRemaining(entry -> orderedMap.put(entry.getKey(), entry.getValue()));
        orderedMap.put("current_units", currentUnits);
        orderedMap.put("current", current);

        // Convert the ordered map back to a JSON string
        ObjectNode reorderedNode = objectMapper.createObjectNode();
        orderedMap.forEach(reorderedNode::set);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reorderedNode);
    }

//    public static void main(String[] args) {
//        OpenMeteoClient client = new OpenMeteoClient();
//        try {
//            String weatherForecast = client.getWeatherForecast();
//            System.out.println(weatherForecast);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}