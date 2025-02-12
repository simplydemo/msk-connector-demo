package io.github.simplydemo.client;

import io.github.simplydemo.utils.Utils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.util.Map;


public class KafakaClientAuth {


    public static final String TOPIC = "HELLO_WORLD";

    private final String SECRET_NAME;

    private AwsCredentialsProvider credentialsProvider;

    private final Utils utils;

    public KafakaClientAuth(final String secretName) {
        this.SECRET_NAME = secretName;
        this.credentialsProvider = DefaultCredentialsProvider.create();
        utils = new Utils(credentialsProvider);
        // this.credentialsProvider = AwsCredentialsProviderChain.create("myProfile");
    }

    public Map<String, String> getSecret() {
        try {
            return utils.getSecrets(SECRET_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            validateCredentials();
            return null;
        }
    }

    public void validateCredentials() {
        try {
            StsClient stsClient = StsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .build();
            GetCallerIdentityRequest request = GetCallerIdentityRequest.builder().build();
            GetCallerIdentityResponse result = stsClient.getCallerIdentity(request);
            System.out.println("STS Authentication Successful!");
            System.out.println("Account: " + result.account());
            System.out.println("UserId: " + result.userId());
            System.out.println("Arn: " + result.arn());
        } catch (Exception e) {
            System.err.println("Error validating AWS credentials: " + e.getMessage());
        }
    }

}
