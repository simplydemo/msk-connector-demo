package io.github.simplydemo.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

public class Utils {

    final AwsCredentialsProvider credentialsProvider;

    public Utils(final AwsCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public Map<String, String> getSecrets(final String secretName) throws Exception {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(credentialsProvider)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
        GetSecretValueResponse getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        String secret = getSecretValueResult.secretString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(secret);

        return Map.of(
                "username", jsonNode.get("username").asText(),
                "password", jsonNode.get("password").asText(),
                "BOOTSTRAP_SERVERS", jsonNode.get("BOOTSTRAP_SERVERS").asText(),
                "BOOTSTRAP_SERVERS.IAM", jsonNode.get("BOOTSTRAP_SERVERS.IAM").asText()
        );
    }

}
