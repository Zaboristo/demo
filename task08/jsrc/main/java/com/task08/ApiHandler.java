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



import java.io.IOException;

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
		runtime = DeploymentRuntime.JAVA11
)
public class ApiHandler implements RequestHandler<Object, String> {

	public static void main(String[] args) throws IOException {
		System.out.println(new ApiHandler().handleRequest(null, null));
	}
	@Override
	public String  handleRequest(Object request, Context context) {
		OpenMeteoClient client = new OpenMeteoClient();

		try {
			return client.getWeatherForecast();
		} catch (Exception e) {
			return "Error";
		}

	}
}