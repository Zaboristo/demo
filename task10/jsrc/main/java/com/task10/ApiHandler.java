package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ExplicitAuthFlowsType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;

import java.util.HashMap;
import java.util.Map;

import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID;

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
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "COGNITO_ID", value = "${pool_name}", valueTransformer = USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "CLIENT_ID", value = "${pool_name}", valueTransformer = USER_POOL_NAME_TO_CLIENT_ID),
		@EnvironmentVariable(key = "COGNITO_NAME", value = "${pool_name}"),
		@EnvironmentVariable(key = "PREFIX", value = "${prefix}"),
		@EnvironmentVariable(key = "SUFFIX", value = "${suffix}"),
		@EnvironmentVariable(key = "TABLES_TABLE", value = "${tables_table}"),
		@EnvironmentVariable(key = "RESERVATIONS_TABLE", value = "${reservations_table}")
})
@LambdaUrlConfig(
		authType = AuthType.NONE
)
public class ApiHandler implements RequestHandler<Map<String, Object>, APIGatewayV2HTTPResponse> {

	static Context context;
	@Override
	public APIGatewayV2HTTPResponse handleRequest(Map<String, Object> input, Context context) {
		this.context = context;
		String path = (String) input.get("path");
		String httpMethod = (String) input.get("httpMethod");
		CognitoIdentityProviderClient provider = CognitoIdentityProviderClient.create();

		switch (path) {
			case "/signup":
				if ("POST".equals(httpMethod)) {
					return new SignUp().handleSignup(input, provider);
				}
				break;
			case "/signin":
				if ("POST".equals(httpMethod)) {
					return new SignIn().handleSignin(input, provider);
				}
				break;
			case "/tables":
				if ("GET".equals(httpMethod)) {
					return new TablesHandler().handleGetTables(input, provider);
				} else if ("POST".equals(httpMethod)) {
					return new TablesHandler().handlePostTables(input, provider);
				}
				break;
			case "/reservations":
				if ("POST".equals(httpMethod)) {
					return new ReservationHandler().handlePostReservations(input, provider);
				} else if ("GET".equals(httpMethod)) {
					return new ReservationHandler().handleGetReservations(input, provider);
				}
				break;
			default:
				return APIGatewayV2HTTPResponse.builder()
						.withStatusCode(400)
						.withBody("Invalid path")
						.build();
		}
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(400)
				.withBody("Invalid request")
				.build();
	}

	public static void createUserPoolApiClientIfNotExist(String cognitoId, String clientName,
														 CognitoIdentityProviderClient cognitoClient,
														 Context context) {
		boolean noOneApiClient = cognitoClient.listUserPoolClients(ListUserPoolClientsRequest.builder()
				.userPoolId(cognitoId)
				.build()).userPoolClients().isEmpty();
		if (noOneApiClient) {
			CreateUserPoolClientResponse createUserPoolClientResponse =
					cognitoClient.createUserPoolClient(CreateUserPoolClientRequest.builder()
							.userPoolId(cognitoId)
							.clientName(clientName)
							.explicitAuthFlows(ExplicitAuthFlowsType.ADMIN_NO_SRP_AUTH)
							.generateSecret(false)
							.build());
			context.getLogger().log("User pool client was created: " + createUserPoolClientResponse);
		}
	}

	public static String getAccessToken(Map<String, Object> input) {
		Map<String, String> headers = (Map<String, String>) input.get("headers");
		return headers.get("Authorization").split(" ")[1];
	}
}
