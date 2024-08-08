package com.task06;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "audit_producer",
		roleName = "audit_producer-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE
)
@DynamoDbTriggerEventSource(targetTable = "Configuration",
		batchSize = 10)
@DependsOn(name = "Configuration",
		resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "Audit",
		resourceType = ResourceType.DYNAMODB_TABLE)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
	@Override
	public Void handleRequest(DynamodbEvent event, Context context) {
		for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
			if ("INSERT".equals(record.getEventName()) || "MODIFY".equals(record.getEventName())) {
				// Convert the DynamoDB stream's AttributeValue to the SDK v2 AttributeValue
				Map<String, AttributeValue> newImage = convertImage(record.getDynamodb().getNewImage());
				Map<String, AttributeValue> oldImage = convertImage(record.getDynamodb().getOldImage());

				String itemKey = newImage.get("key").s();
				String id = UUID.randomUUID().toString();
				String modificationTime = Instant.now().toString();

				Map<String, AttributeValue> auditItem = new HashMap<>();
				auditItem.put("id", AttributeValue.builder().s(id).build());
				auditItem.put("itemKey", AttributeValue.builder().s(itemKey).build());
				auditItem.put("modificationTime", AttributeValue.builder().s(modificationTime).build());

				if ("INSERT".equals(record.getEventName())) {
					auditItem.put("newValue", AttributeValue.builder().m(newImage).build());
				} else if ("MODIFY".equals(record.getEventName())) {
					auditItem.put("updatedAttribute", AttributeValue.builder().s("value").build());
					auditItem.put("oldValue", oldImage.get("value"));
					auditItem.put("newValue", newImage.get("value"));
				}

				PutItemRequest putItemRequest = PutItemRequest.builder()
						.tableName("cmtr-b301d41c-Audit-test")
						.item(auditItem)
						.build();

				dynamoDbClient.putItem(putItemRequest);
			}
		}
		return null;
	}

	private Map<String, AttributeValue> convertImage(Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> image) {
		Map<String, AttributeValue> convertedMap = new HashMap<>();
		if (image != null) {
			image.forEach((key, value) -> convertedMap.put(key, convertAttributeValue(value)));
		}
		return convertedMap;
	}

	private AttributeValue convertAttributeValue(com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue value) {
		AttributeValue.Builder builder = AttributeValue.builder();
		if (value.getS() != null) {
			builder.s(value.getS());
		} else if (value.getN() != null) {
			builder.n(value.getN());
		} else if (value.getB() != null) {
			builder.b(SdkBytes.fromByteBuffer(value.getB()));
		} else if (value.getM() != null) {
			builder.m(convertImage(value.getM()));
		} else if (value.getL() != null) {
			builder.l(value.getL().stream().map(this::convertAttributeValue).toList());
		} else if (value.getNULL() != null) {
			builder.nul(value.getNULL());
		} else if (value.getBOOL() != null) {
			builder.bool(value.getBOOL());
		}
		return builder.build();
	}
}
