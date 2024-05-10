package com.example.mq_kafka.producer;

import com.example.mq_kafka.Consts;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现生产者
 */
public class PartitionProducer {

    /**
     * 消息发送确认
     * 0，只要消息提交到消息缓冲，就视为消息发送成功
     * 1，只要消息发送到分区Leader且写入磁盘，就视为消息发送成功
     * all，消息发送到分区Leader且写入磁盘，同时其他副本分区也同步到磁盘，才视为消息发送成功
     */
    private static final String ACKS_CONFIG = "all";

    /**
     * 缓存消息数达到此数值后批量提交
     */
    private static final String BATCH_SIZE_CONFIG = "1000";

    private static final String MessagePrefix = String.format( "[%s]This is a test message: ", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Consts.BROKER_LIST);
        properties.setProperty(ProducerConfig.ACKS_CONFIG, ACKS_CONFIG);
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE_CONFIG);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer(properties);

        int partition = 14;
        int count = 10;

        for (int i = 0; i < count; i++) {
        for (int j = 0; j < partition; j++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(Consts.TOPIC, j, null, MessagePrefix + i);
                producer.send(record, (recordMetadata, e) -> {
                    if (e != null) {
                        System.out.println("发送消息异常！partition = " + recordMetadata.partition() + "，offset = " + recordMetadata.offset());
                        e.printStackTrace();
                    } else if (recordMetadata != null) {
                        // topic 下可以有多个分区，每个分区的消费者维护一个 offset
                        System.out.println("消息发送成功：partition=" + recordMetadata.partition() + "，offset=" + recordMetadata.offset());
                    }
                });
                Thread.sleep(100L);
            }
        }

        System.out.println("发送完成");
        Thread.sleep(Integer.MAX_VALUE);
    }
}