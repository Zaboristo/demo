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

		String path = request.get("path") == null ? event.getPath() : request.get("path");
		String method = request.get("httpMethod") == null ? event.getHttpMethod() : request.get("httpMethod");

        context.getLogger().log("Received request: urlPath: " + path + ", HTTP method: " + method);

		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();
		String cognitoId = getCognitoIdByName(COGNITO, cognitoClient, context);
		createUserPoolApiClientIfNotExist(cognitoId, COGNITO_CLIENT_API, cognitoClient, context);

		if ("/signup".equals(path)) {
			if ("POST".equals(method)) return new SignUp().handleSignUp(event, context, cognitoClient);
		} else if ("/signin".equals(path)) {
			if ("POST".equals(method)) return new SignIn().handleSignIn(event, context, cognitoClient);
		} else if ("/tables".equals(path)) {
			if ("POST".equals(method)) {
				return new TablesHandler().handleCreateTable(event, context, cognitoClient);
			} else if ("GET".equals(method)) {
				return new TablesHandler().handleGetTables(event, context, cognitoClient);
			}
		} else if (path.matches("/tables/\\d+")) {
			if ("GET".equals(method)) {
				return new TablesHandler().handleGetSpecificTable(event, context, cognitoClient);
			}
		} else if ("/reservations".equals(path)) {
			if ("POST".equals(method)) {
				return new ReservationsHandler().handleCreateReservation(event, context, cognitoClient);
			} else if ("GET".equals(method)) {
				return new ReservationsHandler().handleGetReservations(event, context, cognitoClient);
			}
		}

		context.getLogger().log("Handler for urlPath: " + path + ", and HTTP method: " + method
				+ "was not found");
		throw new RuntimeException("Handler for urlPath: " + path + ", and HTTP method: " + method
				+ "was not found");
	}

}