package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

public class TablesHandler {
    APIGatewayV2HTTPResponse handleGetTables(Map<String, Object> input, CognitoIdentityProviderClient provider) {

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


        if (input.get("path").toString().matches("/tables/\\d+")) {
            return getSpectificTable(input);
        }

        final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(System.getenv("TABLES_TABLE"))
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(scanResponse.items().toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching tables: " + e.getMessage())
                    .build();
        }
    }


    private APIGatewayV2HTTPResponse getSpectificTable(Map<String, Object> input) {

        final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

        String tableId = (String) ((Map<String, Object>) input.get("pathParameters")).get("tableId");

        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().n(tableId).build());

            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(System.getenv("TABLES_TABLE"))
                    .key(key)
                    .build();

            Map<String, AttributeValue> item = dynamoDbClient.getItem(getItemRequest).item();

            if (item == null || item.isEmpty()) {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(404)
                        .withBody("Table not found")
                        .build();
            }

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(item.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("Error fetching table details: " + e.getMessage())
                    .build();
        }
    }


    APIGatewayV2HTTPResponse handlePostTables(Map<String, Object> input, CognitoIdentityProviderClient provider) {
        final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
        Map<String, String> requestBody = (Map<String, String>) input.get("body");

        try {

            provider.getUser(GetUserRequest.builder()
                    .accessToken(ApiHandler.getAccessToken(input))
                    .build());

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().n(requestBody.get("id")).build());
            item.put("number", AttributeValue.builder().n(requestBody.get("number")).build());
            item.put("places", AttributeValue.builder().n(requestBody.get("places")).build());
            item.put("isVip", AttributeValue.builder().bool(Boolean.parseBoolean(requestBody.get("isVip"))).build());

            if (requestBody.containsKey("minOrder")) {
                item.put("minOrder", AttributeValue.builder().n(requestBody.get("minOrder")).build());
            }

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(System.getenv("TABLES_TABLE"))
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody("{\"id\": " + requestBody.get("id") + "}")
                    .build();

        } catch (NotAuthorizedException e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error creating table: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching tables: " + e.getMessage())
                    .build();
        }
    }
}


