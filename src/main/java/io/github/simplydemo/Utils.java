package io.github.simplydemo;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.CredentialsProvider;

import java.util.HashMap;
import java.util.Map;

public class Utils {


    final AWSCredentialsProvider credentialsProvider;

    public Utils(final AWSCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public Map<String, String> getSecrets(final String secretName) throws Exception {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(this.credentialsProvider)
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        String secret = getSecretValueResult.getSecretString();
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
