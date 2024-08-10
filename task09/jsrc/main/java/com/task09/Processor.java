package com.task09;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.xray.AWSXRay;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "processor",
	roleName = "processor-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "table", value = "${target_table}")})
@DependsOn(name = "Weather", resourceType = ResourceType.DYNAMODB_TABLE)
public class Processor implements RequestHandler<Map<String, String>, String> {
private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
		.withRegion(System.getenv("region")).build();
private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
private final Table weatherTable = dynamoDB.getTable("cmtr-b301d41c-" + System.getenv("table") + "-test");

@Override
		public String handleRequest(Map<String, String> event, Context context) {
			String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?...";
			String forecast = getWeatherForecast(weatherApiUrl);

			PutItemRequest request = new PutItemRequest()
					.withTableName(weatherTable.getTableName())
					.addItemEntry("id", new AttributeValue().withS(UUID.randomUUID().toString()))
					.addItemEntry("forecast", new AttributeValue().withS(forecast));

			dynamoDBClient.putItem(request);

			return "Weather data successfully stored!";
		}

	private String getWeatherForecast(String url) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Accept", "application/json")
				.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				// Parse the JSON response
				JSONObject jsonResponse = new JSONObject(response.body());

				// Extract the relevant data as per the schema
				// Adjust this based on the actual response structure
				JSONObject hourly = jsonResponse.getJSONObject("hourly");
				JSONObject hourlyUnits = jsonResponse.getJSONObject("hourly_units");

				JSONObject forecast = new JSONObject();
				forecast.put("elevation", jsonResponse.optDouble("elevation", 0.0));
				forecast.put("generationtime_ms", jsonResponse.optDouble("generationtime_ms", 0.0));
				forecast.put("hourly", new JSONObject()
						.put("temperature_2m", hourly.getJSONArray("temperature_2m"))
						.put("time", hourly.getJSONArray("time")));
				forecast.put("hourly_units", new JSONObject()
						.put("temperature_2m", hourlyUnits.getString("temperature_2m"))
						.put("time", hourlyUnits.getString("time")));
				forecast.put("latitude", jsonResponse.optDouble("latitude", 0.0));
				forecast.put("longitude", jsonResponse.optDouble("longitude", 0.0));
				forecast.put("timezone", jsonResponse.optString("timezone", ""));
				forecast.put("timezone_abbreviation", jsonResponse.optString("timezone_abbreviation", ""));
				forecast.put("utc_offset_seconds", jsonResponse.optInt("utc_offset_seconds", 0));

				return forecast.toString();
			} else {
				throw new RuntimeException("Failed to fetch weather data: " + response.statusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error fetching weather data: " + e.getMessage());
		}
	}
}
