package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReservationHandler {

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

    APIGatewayV2HTTPResponse handleGetReservations(Map<String, Object> input, CognitoIdentityProviderClient provider) {
        try {
            provider.getUser(GetUserRequest.builder()
                    .accessToken(ApiHandler.getAccessToken(input))
                    .build());
        } catch (NotAuthorizedException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching tables: " + e.getMessage())
                    .build();
        }

        try {
            // Create a ScanRequest to retrieve all items from the Reservations table
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(System.getenv("RESERVATIONS_TABLE"))
                    .build();

            // Perform the scan operation
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResponse.items();

            // Convert the items list to a JSON string (using your preferred method or library)
            String jsonResponse = convertItemsToJson(items);

            // Return the response
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(jsonResponse)
                    .build();

        } catch (Exception e) {
            // Handle exceptions and return a 500 error response
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("Error retrieving reservations: " + e.getMessage())
                    .build();
        }
    }

    private String convertItemsToJson(List<Map<String, AttributeValue>> items) {
        StringBuilder json = new StringBuilder("[");
        for (Map<String, AttributeValue> item : items) {
            json.append("{");
            item.forEach((key, value) -> json.append("\"").append(key).append("\": \"").append(value.s()).append("\","));
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("},");
        }
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]");
        return json.toString();
    }



    APIGatewayV2HTTPResponse handlePostReservations(Map<String, Object> input, CognitoIdentityProviderClient provider) {
        try {
            provider.getUser(GetUserRequest.builder()
                    .accessToken(ApiHandler.getAccessToken(input))
                    .build());
        } catch (NotAuthorizedException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching tables: " + e.getMessage())
                    .build();
        }

        try {
            // Parse the input request body
            Map<String, Object> requestBody = (Map<String, Object>) input.get("body");

            // Generate a unique reservation ID
            String reservationId =  UUID.randomUUID().toString();

            // Prepare the item to put in the Reservations table
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("reservationId", AttributeValue.builder().s(reservationId).build());
            item.put("tableNumber", AttributeValue.builder().n(String.valueOf(requestBody.get("tableNumber"))).build());
            item.put("clientName", AttributeValue.builder().s((String) requestBody.get("clientName")).build());
            item.put("phoneNumber", AttributeValue.builder().s((String) requestBody.get("phoneNumber")).build());
            item.put("date", AttributeValue.builder().s((String) requestBody.get("date")).build());
            item.put("slotTimeStart", AttributeValue.builder().s((String) requestBody.get("slotTimeStart")).build());
            item.put("slotTimeEnd", AttributeValue.builder().s((String) requestBody.get("slotTimeEnd")).build());

            // Create a PutItemRequest
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(System.getenv("RESERVATIONS_TABLE"))
                    .item(item)
                    .build();

            // Put the item into the Reservations table
            dynamoDbClient.putItem(putItemRequest);

            // Return the response
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody("{\"reservationId\":\"" + reservationId + "\"}")
                    .build();

        } catch (Exception e) {
            // Handle exceptions and return a 500 error response
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("Error creating reservation: " + e.getMessage())
                    .build();
        }
    }
    }

