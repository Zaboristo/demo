package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import java.util.HashMap;
import java.util.Map;

public class SignIn {
    APIGatewayV2HTTPResponse handleSignin(Map<String, Object> input, CognitoIdentityProviderClient provider) {
        Map<String, String> requestBody = (Map<String, String>) input.get("body");


        try {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", requestBody.get("email"));
            authParameters.put("PASSWORD", requestBody.get("password"));

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(Util.getCognitoID(provider))
                    .authParameters(authParameters)
                    .build();

            InitiateAuthResponse authResponse = provider.initiateAuth(authRequest);

            String idToken = authResponse.authenticationResult().idToken();

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("accessToken", idToken);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error during sign-in: " + e.getMessage())
                    .build();
        }
    }

}
