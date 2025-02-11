package io.github.simplydemo;


import io.debezium.connector.mysql.MySqlConnector;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MySQLConnectorExample {

    public static void main(String[] args) {
        final Properties props = new Properties();
        props.setProperty("name", "engine");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", "/tmp/debezium/offsets.dat");
        props.setProperty("offset.flush.interval.ms", "60000");
        props.setProperty("converter.schemas.enable", "false");
        // Connector
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3308");
        props.setProperty("database.user", "kafkasrc");
        props.setProperty("database.password", "DeMoKafkaSrc1234%%");
        props.setProperty("database.server.id", "175802");
        props.setProperty("database.server.name", "simplydemo-mysqlserver");
        props.setProperty("database.include.list", "demosrc");
        props.setProperty("table.include.list", "demosrc.products");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        props.setProperty("database.history.file.filename", "/tmp/debezium/db_history.dat");

        // Broker
        props.setProperty("bootstrap.servers", "b-1.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9098,b-2.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9098");
        props.setProperty("database.history.kafka.bootstrap.servers", "b-1.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9098,b-2.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9098");
        props.setProperty("schema.history.internal.kafka.bootstrap.servers", "b-1.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9098,b-2.symplydemomsk.175802.c3.kafka.ap-northeast-2.amazonaws.com:9098");
        props.setProperty("topic.prefix", "simply");
        props.setProperty("database.history.kafka.topic", "history.database-changes");
        props.setProperty("schema.history.internal.kafka.topic", "history.schema-changes");
        props.setProperty("time.precision.mode", "connect");
        props.setProperty("heartbeat.interval.ms", "2000");
        props.setProperty("snapshot.locking.mode", "none");
        props.setProperty("skipped.operations", "none");
        props.setProperty("quote.identifiers", "always");
        props.setProperty("decimal.handling.mode", "double");

        props.setProperty("key.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("value.converter", "org.apache.kafka.connect.json.JsonConverter");

        // 불행히도 sasl.jaas.config 와 같은 속성은 ProducerConfig 구성에서 제외 된다.
        // IAM Authentication
        props.setProperty("sasl.mechanism", "AWS_MSK_IAM");
        props.setProperty("sasl.security.protocol", "SASL_SSL");
        props.setProperty("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
        props.setProperty("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

        DebeziumEngine.Builder<ChangeEvent<String, String>> builder = DebeziumEngine.create(Json.class);
        builder.using(props);
        builder.notifying(record -> {
            System.out.println("Key: " + record.key());
            System.out.println("Value: " + record.value());
        });
        builder.using(new DebeziumEngine.CompletionCallback() {
            @Override
            public void handle(boolean isSuccess, String s, Throwable throwable) {
                if (throwable != null) {
                    System.out.println("Result: " + isSuccess + ", Message: " + s);
                }
            }
        });
        DebeziumEngine<ChangeEvent<String, String>> engine = builder.build();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);

        try {
            executor.shutdown();
            while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Waiting another 5 seconds for the embedded engine to shut down");
            }
        } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
        }
    }
}