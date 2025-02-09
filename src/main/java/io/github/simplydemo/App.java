package io.github.simplydemo;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class App {

    public static final String TOPIC = "HELLOWORLD";

    private final String SECRET_NAME;

    public App(final String secretName) {
        this.SECRET_NAME = secretName;
    }

    private final ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("dev-sts");

    private final Utils utils = new Utils(credentialsProvider);

    private HashMap<String, Object> getConfig() throws Exception {
        Map<String, String> secrets = utils.getSecrets(SECRET_NAME);
        // final String BOOTSTRAP_SERVERS = secrets.get("BOOTSTRAP_SERVERS");
        final String BOOTSTRAP_SERVERS = "b-1.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9096";
        final String USERNAME = secrets.get("username");
        final String PASSWORD = secrets.get("password");

        HashMap<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + USERNAME + "\" password=\"" + PASSWORD + "\";");
        return props;
    }

    public void createTopic() throws Exception {
        Map<String, Object> props = getConfig();
        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(TOPIC, 1, (short) 1);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Topic created successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void deleteTopic() throws Exception {
        Map<String, Object> props = getConfig();
        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(TOPIC, 1, (short) 1);
            adminClient.deleteTopics(Collections.singleton(String.valueOf(newTopic))).all().get();
            System.out.println("Topic deleted successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void sendMessage(final String topic, final String key, final String message) throws Exception {
        Map<String, Object> props = getConfig();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
            producer.send(record).get();
            System.out.println("Message sent successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void consumeMessage(final String topic) throws Exception {
        Map<String, Object> props = getConfig();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "MyGroup" + System.currentTimeMillis());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            System.out.println("Subscribed to topic: " + topic);

            int emptyPolls = 0;
            while (emptyPolls < 10) { // 10번의 빈 폴링 후 종료
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    emptyPolls++;
                    System.out.println("No messages received. Empty poll count: " + emptyPolls);
                } else {
                    emptyPolls = 0; // 메시지를 받았으면 카운터 리셋
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.printf("Received message: topic = %s, partition = %d, offset = %d, key = %s, value = %s%n", record.topic(), record.partition(), record.offset(), record.key(), record.value());
                    }
                }
            }
            System.out.println("Consumer finished after 10 empty polls");
        } catch (Exception e) {
            System.out.println("Error occurred while consuming messages:");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        /*
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Received message: key = %s, value = %s%n", record.key(), record.value());
                }
            }
        }
         */
    }

}
