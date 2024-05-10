package com.example.mq_kafka.consumer;

import com.example.mq_kafka.Consts;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Properties;

// 短连接测试
public class ShortConnection {
    private static final String AUTO_COMMIT_INTERVAL_MS_CONFIG = "10";

    private static String clientId = "TelplplplId11";
    private static String groupId = "TesGroupId22";

    private static String autoOffsetResetLatest = "latest";
    private static String autoOffsetResetEarliest = "earliest";

    private static int threadNums = 10;

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Consts.BROKER_LIST);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetEarliest);
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_MS_CONFIG);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        while (true) {
            for (int nums = threadNums; nums > 0; nums--) {
                new Thread(() -> {
                    for (int i = 1; i > 0; i--) {
                        KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);
                        consumer.subscribe(Arrays.asList(Consts.TOPIC2));
                        ConsumerRecords<String, String> records = consumer.poll(500);
                        records.forEach(record -> {
                            System.out.println(clientId + "，分区：" + record.partition() + "，偏移量：" + record.offset() + "，消费消息：" + record.toString());
                        });
                    }
                }).start();
                Thread.sleep(100);
            }
        }

    }

}
