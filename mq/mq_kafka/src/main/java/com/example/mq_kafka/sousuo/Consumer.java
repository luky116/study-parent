package com.example.mq_kafka.sousuo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 官方jar实现消费者
 */
public class Consumer {
    private static final String AUTO_COMMIT_INTERVAL_MS_CONFIG = "1000";

    private static String clientId = "test-liuyuecai-clientId-5555";
    private static String groupId = "test-liuyuecai-groupId-55555";
    private static String autoOffsetResetLatest = "latest";
    private static String autoOffsetResetEarliest = "earliest";
    private static FilesWriter filesWriter;
    private static AtomicLong atomicInteger = new AtomicLong(0);
    private static long last = 0;

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        filesWriter = new FilesWriter();

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER_LIST);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, String.valueOf(5000 * 1024 * 1024));
//        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5000");
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetEarliest);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_MS_CONFIG);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        for (int i = 10; i > 0; i--) {
            new Thread(() -> {
                try {
                    new Consumer().consume(properties);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    public void consume(Properties properties) throws InterruptedException {
        KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);
        consumer.subscribe(Arrays.asList(Config.TOPIC));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            filesWriter.write(records);
            atomicInteger.addAndGet(records.count());
            if (atomicInteger.get() - last > 1000L) {
                last = atomicInteger.get();
                System.out.println("处理数据：" + atomicInteger.get());
            }
//            Thread.sleep(100L);
        }

    }

}