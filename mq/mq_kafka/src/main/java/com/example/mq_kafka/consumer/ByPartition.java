package com.example.mq_kafka.consumer;

import com.example.mq_kafka.Consts;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

/**
 * 官方jar实现消费者
 */
public class ByPartition {

    private static final String AUTO_COMMIT_INTERVAL_MS_CONFIG = "10";

    private static String clientId = "TestHelloClientId";
    private static String groupId = "TestHelloGroupIdqqqqq";
//    private static String groupId = "pc_browser_dd_group_flume_1";
    //    private static String autoOffsetReset = "latest";
    private static String autoOffsetReset = "earliest";

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Consts.BROKER_LIST);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_MS_CONFIG);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);
//        consumer.subscribe(Arrays.asList(Consts.TOPIC));

        TopicPartition partition = new TopicPartition(Consts.TOPIC, 39);
        consumer.assign(Collections.singletonList(partition));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(500);
            records.forEach(record -> {
                consumer.commitAsync();
                System.out.println(clientId + "，分区：" + record.partition() + "，偏移量：" + record.offset() + "，消费消息：" + record.toString());
            });

            Thread.sleep(1000L);
        }

    }

}