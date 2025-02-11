package io.github.simplydemo.client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import io.github.simplydemo.utils.Utils;

import java.util.Map;

public class KafakaClientAuth {

    public static final String TOPIC = "HELLO_WORLD";

    private final String SECRET_NAME;

    private AWSCredentialsProvider credentialsProvider;

    private final Utils utils;

    public KafakaClientAuth(final String secretName) {
        this.SECRET_NAME = secretName;
        this.credentialsProvider = new DefaultAWSCredentialsProviderChain();
        utils = new Utils(credentialsProvider);
        // this.credentialsProvider = new AWSCredentialsProviderChain(new ProfileCredentialsProvider(PROFILE));
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
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .build();
            GetCallerIdentityRequest request = new GetCallerIdentityRequest();
            GetCallerIdentityResult result = stsClient.getCallerIdentity(request);
            System.out.println("STS Authentication Successful!");
            System.out.println("Account: " + result.getAccount());
            System.out.println("UserId: " + result.getUserId());
            System.out.println("Arn: " + result.getArn());
        } catch (Exception e) {
            System.err.println("Error validating AWS credentials: " + e.getMessage());
        }
    }

}
