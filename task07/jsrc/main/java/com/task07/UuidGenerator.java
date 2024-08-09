package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@LambdaHandler(
    lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
public class UuidGenerator implements RequestHandler<ScheduledEvent, String> {

	@Override
	public String handleRequest(ScheduledEvent event, Context context) {
		// Generate 10 UUIDs
		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			uuids.add("'" + UUID.randomUUID().toString() + "'");
		}

		// Get current time in ISO 8601 format
		String currentTime = Instant.now().toString();

		// Prepare the JSON content
		String jsonContent = String.format("{'ids': %s }", uuids.toString());

		// Retrieve the bucket name from environment variables
		String bucketName = System.getenv("target_bucket");

		// Prepare the S3 object request
		S3Client s3 = S3Client.builder().build();
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(currentTime)
				.build();

		Content content = new Content(uuids);
		Gson gson = new Gson();
		gson.toJson(content);

		try {
			s3.putObject(putObjectRequest, RequestBody.fromString(gson.toJson(content)));
		} catch (S3Exception e) {
			context.getLogger().log("Error occurred: " + e.getMessage());
			return "500 Internal Server Error";
		}

		return "200 OK";
	}
}