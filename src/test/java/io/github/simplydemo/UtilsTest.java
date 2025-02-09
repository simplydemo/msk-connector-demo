package io.github.simplydemo;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

@TestMethodOrder(MethodOrderer.MethodName.class) // 메서드 이름 순으로 실행
public class UtilsTest {

    private static final String SECRET_NAME = "AmazonMSK_dev/simplydemo/kylo"; // replace your secretName for MSK

    private static final ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("dev-sts"); // replace your AWS_PROFILE for AWS credentials
    private final Utils utils = new Utils(credentialsProvider);

    @Test
    public void test102_secretsWithName() {
        try {
            Map<String, String> secrets = utils.getSecrets(SECRET_NAME);
            System.out.println(secrets.get("BOOTSTRAP_SERVERS"));
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test103_createTopic() {
        try {
            App app = new App(SECRET_NAME);
            app.createTopic();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test104_sendMessage() {
        try {
            App app = new App(SECRET_NAME);
            app.sendMessage(App.TOPIC, "key", "Hello MSK, This is SCRAM!!!");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test105_consumeMessage() {
        try {
            App app = new App(SECRET_NAME);
            app.consumeMessage(App.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }


    @Test
    public void test_deleteTopic() {
        try {
            App app = new App(SECRET_NAME);
            app.deleteTopic();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

}