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
		String path = event.getPath();
		String httpMethod = event.getHttpMethod();

		// Handle /hello GET request
		if ("/hello".equals(path) && "GET".equals(httpMethod)) {
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");

			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()

                    .withStatusCode(200)
					.withHeaders(headers)
					.withBody("{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}");
			return response;
		} else {
			// Handle other requests
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");

			String errorMessage = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", path, httpMethod);

			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400)
					.withHeaders(headers)
					.withBody("{\"statusCode\": 400, \"message\": \"" + errorMessage + "\"}");
			return response;
		}
	}
}
