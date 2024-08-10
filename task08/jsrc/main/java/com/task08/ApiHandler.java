package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.meteo.OpenMeteoClient;



import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	layers = {"sdk-layer"},
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
@LambdaLayer(layerName = "sdk-layer",
		libraries = "lib/weather-sdk-1.0.jar",
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.JAR)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		OpenMeteoClient client = new OpenMeteoClient();
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		try {
			String weatherData = client.getWeatherForecast();
			response.setStatusCode(200);
			response.setBody(weatherData);
		} catch (Exception e) {
			response.setStatusCode(500);
			response.setBody("Error fetching weather data: " + e.getMessage());
		}

		return response;
	}
}