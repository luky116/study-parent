package com.example.mq_kafka.consumer;

import com.example.mq_kafka.Consts;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Utils;

import java.util.Arrays;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.DAYS;

/**
 * 官方jar实现消费者
 */
public class ByCommon2 {

    private static final String AUTO_COMMIT_INTERVAL_MS_CONFIG = "10";

    private static String clientId = "T12e";
    private static String groupId = "myGroupTestV11111";
//    private static String groupId = "1700210850sds21212121";
    private static String autoOffsetResetLatest = "latest";
    private static String autoOffsetResetEarliest = "earliest";
    static int i = 1;


    public static void main(String[] args) throws InterruptedException {
        int aa = Utils.abs(groupId.hashCode())%60;
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Consts.BROKER_LIST);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetLatest);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_MS_CONFIG);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        for (; i > 0; i--) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);
                    consumer.subscribe(Arrays.asList(Consts.TOPIC2));
                    while (true) {
                        ConsumerRecords<String, String> records = consumer.poll(500);
                        records.forEach(record -> {
                            System.out.println(clientId + "，分区：" + record.partition() + "，偏移量：" + record.offset() + "，消费消息：" + record.toString());
                        });
                    }
                }
            }).start();
        }

        Thread.sleep(DAYS.ordinal());

//        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(500);
//            records.forEach(record -> {
//                System.out.println(clientId + "，分区：" + record.partition() + "，偏移量：" + record.offset() + "，消费消息：" + record.toString());
//            });
//
//            Thread.sleep(100L);
//        }

    }

}