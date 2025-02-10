package io.github.simplydemo.client;

import io.github.simplydemo.App;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KafkaIAMClient {
    private final App app;
    final Map<String, String> secrets;

    public KafkaIAMClient(App app) {
        this.app = app;
        this.secrets = app.getSecret();
    }

    private static HashMap<String, Object> getConfig(String bootstrapServers) {
        HashMap<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // IAM 인증 설정
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required;");
        props.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        // admin
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "20000");
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "25000");

        // producer
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // consumer
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-" + System.currentTimeMillis()); // 유니크한 그룹 ID
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    public void createTopic() {
        final String bootstrapServers = secrets.get("BOOTSTRAP_SERVERS.IAM");
        final HashMap<String, Object> props = getConfig(bootstrapServers);
        final NewTopic newTopic = new NewTopic(App.TOPIC, 1, (short) 1);
        try (final AdminClient adminClient = AdminClient.create(props)) {
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Topic created successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void deleteTopics(String... topicNames) throws Exception {
        final String bootstrapServers = secrets.get("BOOTSTRAP_SERVERS.IAM");
        final HashMap<String, Object> props = getConfig(bootstrapServers);
        try (final AdminClient adminClient = AdminClient.create(props)) {
            final DeleteTopicsResult result = adminClient.deleteTopics(Arrays.asList(topicNames));
            // 모든 토픽 삭제 작업이 완료될 때까지 대기
            for (final String topicName : topicNames) {
                final KafkaFuture<Void> future = result.values().get(topicName);
                try {
                    future.get(); // 삭제 작업이 완료될 때까지 블록
                    System.out.println("Topic " + topicName + " deleted successfully.");
                } catch (ExecutionException e) {
                    System.err.println("Failed to delete topic " + topicName + ": " + e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Topic deletion was interrupted: " + e.getMessage());
        }
    }

    public void sendMessage(final String topic, final String key, final String message) throws Exception {

        final String bootstrapServers = secrets.get("BOOTSTRAP_SERVERS.IAM");
        final HashMap<String, Object> props = getConfig(bootstrapServers);

        try (final KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
            producer.send(record).get();
            System.out.println("Message sent successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void consumeMessage(final String topic) throws Exception {

        final String bootstrapServers = secrets.get("BOOTSTRAP_SERVERS.IAM");
        final HashMap<String, Object> props = getConfig(bootstrapServers);
        try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            System.out.println("Subscribed to topic: " + topic);

            int emptyPolls = 0;
            while (emptyPolls < 3) { // 3번 폴링 후 종료
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(2000));
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

    }
}