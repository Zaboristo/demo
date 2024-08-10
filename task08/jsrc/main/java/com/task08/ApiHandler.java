package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.RetentionSetting;
import com.meteo.OpenMeteoClient;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	layers = {"sdk-layer"}
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
@LambdaLayer(layerName = "sdk-layer",
		libraries = "lib/weather-sdk-1.0.jar",
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64, Architecture.X86_64},
		artifactExtension = ArtifactExtension.ZIP)
public class ApiHandler implements RequestHandler<Object, String> {

	@Override
	public String  handleRequest(Object request, Context context) {
		OpenMeteoClient client = new OpenMeteoClient();

		try {
			String weatherData = client.getWeatherForecast();
			return weatherData;
		} catch (Exception e) {
			return "Error";
		}

	}
}