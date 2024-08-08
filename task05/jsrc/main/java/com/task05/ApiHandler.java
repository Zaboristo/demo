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

			int principalId = (int) input.get("principalId");
			Map<String, String> content = (Map<String, String>) input.get("content");

			Event event = new Event(principalId, content);

			String createdAt = Instant.now().toString();
			Item item = new Item()
					.withPrimaryKey("id", event.getId())
					.withNumber("principalId", event.getPrincipalId())
					.withString("createdAt", event.getCreatedAt())
					.withMap("body", event.getBody());

			Table table = dynamoDB.getTable("cmtr-b301d41c-" + TABLE_NAME + "-test");
			table.putItem(item);

			response.put("statusCode", 201);
			response.put("event", item.toJSON());

		} catch (Exception e) {
			e.printStackTrace();
			response.put("statusCode", 500);
			response.put("error", "Internal Server Error");
		}

		return response;
	}
}