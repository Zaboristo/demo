package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;

import java.time.Instant;
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

	private static final String TABLE_NAME = "Events";
	private final DynamoDB dynamoDB;

	public ApiHandler() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("eu-central-1").build();
		this.dynamoDB = new DynamoDB(client);
	}

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
		Map<String, Object> response = new HashMap<>();
		try {
			// Generate a UUID for the event
			String id = UUID.randomUUID().toString();

			// Extract the principalId and content from the input
			int principalId = (int) input.get("principalId");
			Map<String, String> content = (Map<String, String>) input.get("content");

			// Create the event item to be saved in DynamoDB
			String createdAt = Instant.now().toString();
			Item item = new Item()
					.withPrimaryKey("id", id)
					.withNumber("principalId", principalId)
					.withString("createdAt", createdAt)
					.withMap("body", content);

			// Save the event to the DynamoDB table
			Table table = dynamoDB.getTable(TABLE_NAME);
			table.putItem(item);

			// Create the event map for the response
//			Map<String, Object> event = new HashMap<>();
//			event.put("id", id);
//			event.put("principalId", principalId);
//			event.put("createdAt", createdAt);
//			event.put("body", content);

			// Create the response object
			response.put("statusCode", 201);
			response.put("event", item);

		} catch (Exception e) {
			e.printStackTrace();
			response.put("statusCode", 500);
			response.put("error", "Internal Server Error");
		}

		return response;
	}
}