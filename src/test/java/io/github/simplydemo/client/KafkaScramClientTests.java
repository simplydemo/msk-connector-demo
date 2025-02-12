package io.github.simplydemo.client;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class KafkaScramClientTests {

    private static final String PROFILE = "dev-sts"; // replace your secretName for MSK
    private static final String SECRET_NAME = "AmazonMSK_dev/simplydemo/kylo"; // replace your secretName for MSK

    private static final KafkaScramClientTests.Holder HOLDER = new KafkaScramClientTests.Holder();

    static class Holder {
        final KafakaClientAuth app = new KafakaClientAuth(SECRET_NAME);
        final KafkaScramClient CLIENT;

        public Holder() {
            System.setProperty("aws.profile", PROFILE);
            CLIENT = new KafkaScramClient(app);
        }

        public KafkaScramClient getInstance() {
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
            Map<String, Object> secrets = HOLDER.getInstance().getConfig();
            System.out.println(secrets);
            assertNotNull(secrets);
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
            HOLDER.getInstance().sendMessage(KafakaClientAuth.TOPIC, "key", "Hello MSK, This is SCRAM!!!");
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
    public void test106_produceAndConsumeMessage() {
        try {
            HOLDER.getInstance().sendMessage(KafakaClientAuth.TOPIC, "key2", "Hello MSK, This is SCRAM MESSAGE TEST for the both producer and consume!!!");
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
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