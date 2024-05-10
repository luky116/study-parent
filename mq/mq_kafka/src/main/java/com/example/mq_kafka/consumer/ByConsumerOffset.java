package com.example.mq_kafka.consumer;

import com.example.mq_kafka.Consts;
import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Properties;

/**
 * 官方jar实现消费者
 */
public class ByConsumerOffset {

    private static final String AUTO_COMMIT_INTERVAL_MS_CONFIG = "10";

    private static String clientId = "TestHelloClientId";
    private static String groupId = "TestHelloGroupIdqqqqq";
    //    private static String groupId = "pc_browser_dd_group_flume_1";
    //    private static String autoOffsetReset = "latest";
    private static String autoOffsetReset = "earliest";

    // 想要跟踪的 topic
    private static String originToPic = "os_adv_track_prod";

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Consts.BROKER_LIST);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_MS_CONFIG);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer(properties);
//        consumer.subscribe(Arrays.asList(Consts.TOPIC));

        TopicPartition partition = new TopicPartition(Consts.ConsumerOffsetTopic, Math.abs(originToPic.hashCode()) % 50);
        consumer.assign(Collections.singletonList(partition));


        // 数据格式如下：
        // [mini-test-121i222,mini-spider-douyin-input,2]::[OffsetMetadata[1032033,NO_METADATA],CommitTime 1695367768265,ExpirationTime 1695454168265]
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(500);
            records.forEach(record -> {

//                这里直接将record的全部信息写到System.out打印流中
                GroupMetadataManager.OffsetsMessageFormatter formatter = new GroupMetadataManager.OffsetsMessageFormatter();
//                formatter.writeTo(record, System.out);

                //对record的key进行解析，注意这里的key有两种OffsetKey和GroupMetaDataKey
                //GroupMetaDataKey中只有消费者组ID信息，OffsetKey中还包含了消费的topic信息
                BaseKey key = GroupMetadataManager.readMessageKey(ByteBuffer.wrap(record.key().getBytes()));
                if (key instanceof OffsetKey) {
                    GroupTopicPartition pp = (GroupTopicPartition) key.key();
                    // topic 信息
                    String topic = pp.topicPartition().topic();
                    // 消费组信息
                    String group = pp.group();

                    System.out.println("group : " + group + "  topic : " + topic);
                    System.out.println(key.toString());
                } else if (key instanceof GroupMetadataKey) {
                    System.out.println("groupMetadataKey:------------ "+key.key());
                }

                //对record的value进行解析
//                OffsetAndMetadata om = GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(record.value()));
//                System.out.println(om.toString());


//                consumer.commitAsync();
//
//                GroupMetadataManager.OffsetsMessageFormatter formatter = new GroupMetadataManager.OffsetsMessageFormatter();
//
//                formatter.writeTo(record, System.out);
//
//                System.out.println(clientId + "，分区：" + record.partition() + "，偏移量：" + record.offset() + "，消费消息：" + record.toString());
            });

            Thread.sleep(1000L);
        }

    }

}