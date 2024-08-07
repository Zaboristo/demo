package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final DynamoDB dynamoDB;
	private final Table table;
	private final TimeBasedGenerator uuidGenerator;

	public ApiHandler() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		this.dynamoDB = new DynamoDB(client);
		this.table = dynamoDB.getTable("Events");
		this.uuidGenerator = Generators.timeBasedGenerator();
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		Map<String, Object> requestBody = parseRequestBody(request.getBody());
		String principalId = (String) requestBody.get("principalId");
		Map<String, String> content = (Map<String, String>) requestBody.get("content");

		String eventId = uuidGenerator.generate().toString();
		String createdAt = Instant.now().toString();

		Item item = new Item()
				.withPrimaryKey("id", eventId)
				.withNumber("principalId", Integer.parseInt(principalId))
				.withString("createdAt", createdAt)
				.withMap("body", content);

		PutItemOutcome outcome = table.putItem(item);

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("statusCode", 201);
		responseBody.put("event", item.asMap());

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(201);
		response.setBody(serializeResponseBody(responseBody));

		return response;
	}

	private Map<String, Object> parseRequestBody(String body) {
		// Implement your JSON parsing logic here
		return new HashMap<>();
	}

	private String serializeResponseBody(Map<String, Object> body) {
		// Implement your JSON serialization logic here
		return "";
	}
}
