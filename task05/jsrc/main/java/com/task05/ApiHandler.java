package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;


import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
@DependsOn(name = "Events",
		resourceType = ResourceType.DYNAMODB_TABLE)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	private final DynamoDbClient dynamoDbClient;
	private final ObjectMapper objectMapper;

	public ApiHandler() {
		this.dynamoDbClient =  DynamoDbClient.create();
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
		try {
			// Parse input
			String bodyString = (String) input.get("body");
			Map<String, Object> body = objectMapper.readValue(bodyString, Map.class);
			int principalId = (Integer) body.get("principalId");
			Map<String, String> content = (Map<String, String>) body.get("content");

			// Create event
			Event event = new Event(principalId, content);

			// Save event to DynamoDB
			Map<String, AttributeValue> item = new HashMap<>();
			item.put("id", AttributeValue.builder().s(event.getId()).build());
			item.put("principalId", AttributeValue.builder().n(String.valueOf(event.getPrincipalId())).build());
			item.put("createdAt", AttributeValue.builder().s(event.getCreatedAt()).build());
			item.put("body", AttributeValue.builder().m(convertMap(content)).build());

			PutItemRequest request = PutItemRequest.builder()
					.tableName("Events")
					.item(item)
					.build();

			dynamoDbClient.putItem(request);

			// Prepare response
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("statusCode", 201);
			responseBody.put("event", objectMapper.writeValueAsString(event));

			return responseBody;

		} catch (JsonProcessingException e) {
			e.printStackTrace();

			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 500);
			errorResponse.put("error", "JSON parsing error: " + e.getMessage());
			return errorResponse;
		} catch (DynamoDbException e) {
			e.printStackTrace();

			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 500);
			errorResponse.put("error", "DynamoDB error: " + e.getMessage());
			return errorResponse;
		} catch (Exception e) {
			e.printStackTrace();

			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 500);
			errorResponse.put("error", "General error: " + e.getMessage());
			return errorResponse;
		}
	}

	private Map<String, AttributeValue> convertMap(Map<String, String> map) {
		Map<String, AttributeValue> result = new HashMap<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			result.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
		}
		return result;
	}
}
