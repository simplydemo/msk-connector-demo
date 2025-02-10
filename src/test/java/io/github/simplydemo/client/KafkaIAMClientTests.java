package io.github.simplydemo.client;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import io.github.simplydemo.utils.Utils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.MethodName.class) // 메서드 이름 순으로 실행
public class KafkaIAMClientTests {

    private static final Holder CONFIG = new KafkaIAMClientTests.Holder();

    static class Holder {
        public Holder() {
            System.setProperty("aws.profile", PROFILE);
        }
    }

    private static final String PROFILE = "dev-sts"; // replace your secretName for MSK
    private static final String SECRET_NAME = "AmazonMSK_dev/simplydemo/kylo"; // replace your secretName for MSK


    // private static final ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("dev-sts"); // replace your AWS_PROFILE for AWS credentials
    // private final Utils utils = new Utils(credentialsProvider);

    final KafakaClientAuth app = new KafakaClientAuth(PROFILE, SECRET_NAME);

    final KafkaIAMClient client = new KafkaIAMClient(app);

    @Test
    public void test101_init() {
        assertNotNull(app);
    }

    @Test
    public void test102_secretsWithName() {
        try {
            Map<String, String> secrets = app.getSecret();
            System.out.println(secrets.get("BOOTSTRAP_SERVERS.IAM"));
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test103_createTopic() {
        try {
            client.createTopic();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test104_sendMessage() {
        try {
            client.sendMessage(KafakaClientAuth.TOPIC, "key", "Hello MSK, This is SCRAM!!!");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test105_consumeMessage() {
        try {
            client.consumeMessage(KafakaClientAuth.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }


    @Test
    public void test_deleteTopics() {
        try {
            client.deleteTopics(KafakaClientAuth.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

}