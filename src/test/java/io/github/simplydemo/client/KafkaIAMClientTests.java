package io.github.simplydemo.client;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import io.github.simplydemo.App;
import io.github.simplydemo.Utils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.MethodName.class) // 메서드 이름 순으로 실행
public class KafkaIAMClientTests {

    private static final String PROFILE = "dev-sts"; // replace your secretName for MSK
    private static final String SECRET_NAME = "AmazonMSK_dev/simplydemo/kylo"; // replace your secretName for MSK

    private static final ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("dev-sts"); // replace your AWS_PROFILE for AWS credentials
    private final Utils utils = new Utils(credentialsProvider);

    final App app = new App(PROFILE, SECRET_NAME);

    final KafkaIAMClient client = new KafkaIAMClient(app);

    @Test
    public void test101_init() {
        assertNotNull(app);
    }

    @Test
    public void test102_secretsWithName() {
        try {
            Map<String, String> secrets = utils.getSecrets(SECRET_NAME);
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
            client.sendMessage(App.TOPIC, "key", "Hello MSK, This is SCRAM!!!");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test105_consumeMessage() {
        try {
            client.consumeMessage(App.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }


    @Test
    public void test_deleteTopics() {
        try {
            client.deleteTopics(App.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

}