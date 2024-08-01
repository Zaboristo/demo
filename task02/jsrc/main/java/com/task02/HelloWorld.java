package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;

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
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        String path = requestEvent.getRequestContext().getHttp().getPath();
        String method = requestEvent.getRequestContext().getHttp().getMethod();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if ("/hello".equals(path) && "GET".equalsIgnoreCase(method)) {
            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(200);
            response.setBody("{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}");
            return response;
        } else {
            String errorMessage = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", "/cmtr-b301d41c", "GET");
            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(400);
            response.setBody("{\"statusCode\": 400, \"message\": \"" + errorMessage + "\"}");
            return response;
        }
    }
}
