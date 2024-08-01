package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
public class HelloWorld implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		// Log the entire input event for debugging
		context.getLogger().log("Input event: " + event.toString());

		// Initialize path and httpMethod with null checks
		String path = event.getPath() != null ? event.getPath() : "null";
		String httpMethod = event.getHttpMethod() != null ? event.getHttpMethod() : "null";

		// Log path and httpMethod for debugging
		context.getLogger().log("Request path: " + path);
		context.getLogger().log("HTTP method: " + httpMethod);

		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");

		// Handle /hello GET request
		if (path.contains("/hello") && "GET".equals(httpMethod)) {
			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withHeaders(headers)
					.withBody("{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}");
			return response;
		} else {
			// Handle other requests
			String errorMessage = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", "/cmtr-b301d41c", "GET");

			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400)
					.withHeaders(headers)
					.withBody("{\"statusCode\": 400, \"message\": \"" + errorMessage + "\"}");
			return response;
		}
	}
}
