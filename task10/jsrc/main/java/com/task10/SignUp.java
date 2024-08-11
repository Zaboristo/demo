package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class SignUp {
    APIGatewayV2HTTPResponse handleSignup(Map<String, Object> input, CognitoIdentityProviderClient provider) {
        Map<String, String> requestBody = (Map<String, String>) input.get("body");

        try {
            AdminCreateUserResponse result = provider.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(System.getenv("COGNITO_ID"))
                    .username(requestBody.get("email"))
                    .temporaryPassword(requestBody.get("password"))
                    .messageAction(MessageActionType.SUPPRESS)
                    .userAttributes(
                            AttributeType.builder().name("email").value(requestBody.get("email")).build(),
                            AttributeType.builder().name("given_name").value(requestBody.get("firstName")).build(),
                            AttributeType.builder().name("family_name").value(requestBody.get("lastName")).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .build());

            provider.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(System.getenv("COGNITO_ID"))
                    .username(requestBody.get("email"))
                    .password(requestBody.get("password"))
                    .permanent(true)
                    .build());

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody("Sign-up process is successful")
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error during sign-up: " + e.getMessage())
                    .build();
        }
    }
}

