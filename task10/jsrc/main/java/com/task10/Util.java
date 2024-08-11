package com.task10;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

import java.util.List;

public class Util {
    static String getCognitoID(CognitoIdentityProviderClient provider) {

            ListUserPoolsResponse listUserPoolsResponse = provider.listUserPools(ListUserPoolsRequest.builder().build());
            List<UserPoolDescriptionType> userPools = listUserPoolsResponse.userPools();
            for (UserPoolDescriptionType userPool : userPools) {
                if (System.getenv("COGNITO_NAME").equals(userPool.name())) {
                    String cognitoId = userPool.id();
                    return cognitoId;
                }
            }
            return null;
        }
    }

