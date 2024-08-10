package com.task08;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;


public class OpenMeteoClient {

    public static void main(String[] args) throws Exception {
        System.out.println(new OpenMeteoClient().getWeatherForecast());
    }

    public String getWeatherForecast() throws IOException {
        URL var1 = new URL("https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30.5&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m");
        Scanner var2 = new Scanner((InputStream)var1.getContent());
        StringBuilder var3 = new StringBuilder();

        while(var2.hasNext()) {
            var3.append(var2.nextLine());
        }

        return var3.toString();
    }
//        // Create a URL object with the API URL
//        URL url = new URL(API_URL);
//
//        // Open a connection to the URL
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//
//        // Get the response from the API
//        Scanner scanner = new Scanner(connection.getInputStream());
//        StringBuilder response = new StringBuilder();
//        while (scanner.hasNext()) {
//            response.append(scanner.nextLine());
//        }
//        scanner.close();
//
//        // Parse the JSON response
//        JsonNode jsonNode = objectMapper.readTree(response.toString());
//
//        // Extract the `current_units` and `current` fields
//        JsonNode currentUnits = jsonNode.get("current_units");
//        JsonNode current = jsonNode.get("current");
//
//        // Remove `current_units` and `current` from the main JSON
//        ((ObjectNode) jsonNode).remove("current_units");
//        ((ObjectNode) jsonNode).remove("current");
//
//        // Create a new JSON object in the desired order
//        Map<String, JsonNode> orderedMap = new LinkedHashMap<>();
//        jsonNode.fields().forEachRemaining(entry -> orderedMap.put(entry.getKey(), entry.getValue()));
//        orderedMap.put("current_units", currentUnits);
//        orderedMap.put("current", current);
//
//        // Convert the ordered map back to a JSON string
//        ObjectNode reorderedNode = objectMapper.createObjectNode();
//        orderedMap.forEach(reorderedNode::set);
//
//        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reorderedNode);
//    }

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