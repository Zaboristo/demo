package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.FunctionResponseType;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(targetQueue = "async_queue",
batchSize = 123,
functionResponseTypes = FunctionResponseType.REPORT_BATCH_ITEM_FAILURES)
@DependsOn(name = "async_queue",
resourceType = ResourceType.SQS_QUEUE)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
public class SqsHandler implements RequestHandler<SQSEvent, Void> {
	public Void handleRequest(SQSEvent event, Context context) {
		event.getRecords().forEach(record -> {
			context.getLogger().log("SQS Message: " + record.getBody());
		});
		return null;
	}
}
