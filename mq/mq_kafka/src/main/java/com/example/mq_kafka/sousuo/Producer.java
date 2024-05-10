package com.example.mq_kafka.sousuo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * 实现生产者
 */
public class Producer {

    private static final int count = 1000000;
    private static final String ACKS_CONFIG = "1";
    private static final String BATCH_SIZE_CONFIG = "1000";

    public static void main(String[] args) throws InterruptedException {

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER_LIST);
        properties.setProperty(ProducerConfig.ACKS_CONFIG, ACKS_CONFIG);
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE_CONFIG);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer(properties);

        for (int i = 0; i < count; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(Config.TOPIC, i + "");
            producer.send(record, (recordMetadata, e) -> {
                if (e != null) {
                    System.out.println("发送消息异常！" + e);
                } else if (recordMetadata != null) {
                    System.out.println("消息发送成功：partition=" + recordMetadata.partition() + "，offset=" + recordMetadata.offset());
                }
            });
            if (i% 1000 == 0) {
                Thread.sleep(50L);
            }
        }

        System.out.println("发送完成");
        Thread.sleep(Integer.MAX_VALUE);
    }
}