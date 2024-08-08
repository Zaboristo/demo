package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

	private static final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
	private static final String TABLE_NAME = "Events";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
		try {
			int principalId = Integer.parseInt(event.get("principalId").toString());
			Map<String, String> content = (Map<String, String>) event.get("content");

			String eventId = UUID.randomUUID().toString();
			Instant now = Instant.now();
			String createdAt = DateTimeFormatter.ISO_INSTANT.format(now);

			Map<String, AttributeValue> item = new HashMap<>();
			item.put("id", new AttributeValue().withS(eventId));
			item.put("principalId", new AttributeValue().withN(Integer.toString(principalId)));
			item.put("createdAt", new AttributeValue().withS(createdAt));
			item.put("body", new AttributeValue().withM(objectMapper.convertValue(content, Map.class)));

			PutItemRequest putItemRequest = new PutItemRequest()
					.withTableName(TABLE_NAME)
					.withItem(item);

			PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 201);
			response.put("event", item);
			return response;
		} catch (Exception e) {
			// Handle exceptions
			e.printStackTrace();
			return Map.of("statusCode", 500, "error", e.getMessage());
		}
	}
}
