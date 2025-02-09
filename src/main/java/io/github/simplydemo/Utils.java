package io.github.simplydemo;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class Utils {

    private static final String SECRET_NAME = "AmazonMSK_dev/simplydemo/kylo";

    public Map<String, Object> loadSecretValues(final String secretName) throws Exception {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.defaultClient();
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        String secret = getSecretValueResult.getSecretString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(secret);

//                USERNAME = jsonNode.get("username").asText();
//                PASSWORD = jsonNode.get("password").asText();
//                TOPIC = jsonNode.get("topic").asText();
//                BOOTSTRAP_SERVERS = jsonNode.get("broker.endpoint").asText();

        return Map.of(
                "username", jsonNode.get("username").asText(),
                "password", jsonNode.get("username").asText()
        );
    }
}

