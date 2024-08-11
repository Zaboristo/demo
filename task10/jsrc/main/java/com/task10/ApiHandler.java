package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.task10.LambdaHelper.createUserPoolApiClientIfNotExist;
import static com.task10.LambdaHelper.getCognitoIdByName;
import static com.task10.LambdaVariables.COGNITO;
import static com.task10.LambdaVariables.COGNITO_CLIENT_API;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	runtime = DeploymentRuntime.JAVA17,
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED

)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${pool_name}")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Tables")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Reservations")

public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> request = null;
		try {
			request = objectMapper.readValue(objectMapper.writeValueAsString(event), LinkedHashMap.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		String path = (String) request.get("path");
		String method = (String) request.get("httpMethod");

		String urlPath = path;
		String httpMethod = method;
		context.getLogger().log("Received request: urlPath: " + urlPath + ", HTTP method: " + httpMethod);

		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();
		String cognitoId = getCognitoIdByName(COGNITO, cognitoClient, context);
		createUserPoolApiClientIfNotExist(cognitoId, COGNITO_CLIENT_API, cognitoClient, context);

		if ("/signup".equals(urlPath)) {
			if ("POST".equals(httpMethod)) return new SignUp().handleSignUp(event, context, cognitoClient);
		} else if ("/signin".equals(urlPath)) {
			if ("POST".equals(httpMethod)) return new SignIn().handleSignIn(event, context, cognitoClient);
		} else if ("/tables".equals(urlPath)) {
			if ("POST".equals(httpMethod)) {
				return new TablesHandler().handleCreateTable(event, context, cognitoClient);
			} else if ("GET".equals(httpMethod)) {
				return new TablesHandler().handleGetTables(event, context, cognitoClient);
			}
		} else if (urlPath.matches("/tables/\\d+")) {
			if ("GET".equals(httpMethod)) {
				return new TablesHandler().handleGetSpecificTable(event, context, cognitoClient);
			}
		} else if ("/reservations".equals(urlPath)) {
			if ("POST".equals(httpMethod)) {
				return new ReservationsHandler().handleCreateReservation(event, context, cognitoClient);
			} else if ("GET".equals(httpMethod)) {
				return new ReservationsHandler().handleGetReservations(event, context, cognitoClient);
			}
		}

		context.getLogger().log("Handler for urlPath: " + urlPath + ", and HTTP method: " + httpMethod
				+ "was not found");
		throw new RuntimeException("Handler for urlPath: " + urlPath + ", and HTTP method: " + httpMethod
				+ "was not found");
	}

}