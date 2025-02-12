package io.github.simplydemo.client;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.MethodName.class) // 메서드 이름 순으로 실행
public class KafkaIAMClientTests {

    private static final String PROFILE = "dev-sts"; // replace your secretName for MSK

    private static final String SECRET_NAME = "AmazonMSK_dev/simplydemo/kylo"; // replace your secretName for MSK

    private static final Holder HOLDER = new KafkaIAMClientTests.Holder();

    static class Holder {
        final KafakaClientAuth app = new KafakaClientAuth(SECRET_NAME);
        final KafkaIAMClient CLIENT;

        public Holder() {
            System.setProperty("aws.profile", PROFILE);
            CLIENT = new KafkaIAMClient(app);
        }

        public KafkaIAMClient getInstance() {
            return this.CLIENT;
        }
    }


    @Test
    public void test101_init() {
        assertNotNull(HOLDER.app);
        assertNotNull(HOLDER.getInstance());
    }

    @Test
    public void test102_secretsWithName() {
        try {
            Map<String, String> secrets = HOLDER.app.getSecret();
            System.out.println(secrets.get("BOOTSTRAP_SERVERS.IAM"));
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test103_createTopic() {
        try {
            HOLDER.getInstance().createTopic();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test104_sendMessage() {
        try {
            HOLDER.getInstance().sendMessage(KafakaClientAuth.TOPIC, "key", "Hello MSK, This is IAM!!!");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test105_consumeMessage() {
        try {
            HOLDER.getInstance().consumeMessage(KafakaClientAuth.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Test
    public void test_deleteTopics() {
        try {
            HOLDER.getInstance().deleteTopics(KafakaClientAuth.TOPIC);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

}