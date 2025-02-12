package io.github.simplydemo.client;

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
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaScramClient {
    private final KafakaClientAuth app;

    public KafkaScramClient(KafakaClientAuth app) {
        this.app = app;
    }

    public HashMap<String, Object> getConfig() throws Exception {
        final Map<String, String> secrets = app.getSecret();
        final String USERNAME = secrets.get("username");
        final String PASSWORD = secrets.get("password");
        HashMap<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", secrets.get("BOOTSTRAP_SERVERS"));
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + USERNAME + "\" password=\"" + PASSWORD + "\";");
        return props;
    }

    public void createTopic() throws Exception {
        HashMap<String, Object> props = getConfig();
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "60000");
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "60000");
        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(KafakaClientAuth.TOPIC, 1, (short) 1);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Topic created successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void deleteTopics(String... topicNames) throws Exception {
        HashMap<String, Object> props = getConfig();
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "60000");
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "60000");
        try (AdminClient adminClient = AdminClient.create(props)) {
            DeleteTopicsResult result = adminClient.deleteTopics(Arrays.asList(topicNames));
            // 모든 토픽 삭제 작업이 완료될 때까지 대기
            for (String topicName : topicNames) {
                KafkaFuture<Void> future = result.values().get(topicName);
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
        Map<String, Object> props = getConfig();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put("metadata.max.age.ms", "5000");  // 5초마다 메타데이터 리프레시

        try (final KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

            Future<RecordMetadata> future = producer.send(record);
            future.get(); // 메시지가 성공적으로 전송될 때까지 대기
            Thread.sleep(3000); // 추가적으로 3초 대기
            System.out.println("Message sent successfully");
        } catch (InterruptedException | ExecutionException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void consumeMessage(final String topic) throws Exception {
        final Map<String, Object> props = getConfig();
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "10000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "MyFixedGroup");  // 고정 그룹 ID
        // props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");  // 최근 한 번에 최대 10 건씩 가져옮
        // props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "NewGroup" + System.currentTimeMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 첨부터 1건씩
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "MyConsumer-" + UUID.randomUUID());

        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(Collections.singletonList(topic));
                System.out.println("Subscribed to topic: " + topic);
                int attempts = 0;
                while (attempts < 5) { // 최대 5번 시도
                    final ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1)); // 1초 동안 대기
                    if (!records.isEmpty()) {
                        for (ConsumerRecord<String, String> record : records) {
                            System.out.printf("Received message: topic = %s, partition = %d, offset = %d, key = %s, value = %s%n",
                                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
                        }
                        break;
                    } else {
                        System.out.println("No messages received. Attempt: " + (attempts + 1));
                        attempts++;
                    }
                }
                if (attempts == 5) {
                    System.out.println("No messages received after 10 attempts.");
                }
            } catch (Exception e) {
                System.out.println("Error occurred while consuming messages:");
                e.printStackTrace();
            }
            break;
        }
    }

}
