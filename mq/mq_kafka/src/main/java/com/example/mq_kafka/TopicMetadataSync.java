package com.example.mq_kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.Serdes;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * topic元数据迁移
 */
public class TopicMetadataSync {
    /**
     * 最大分区上限
     */
    private static final int MAX_NUM_PARTITIONS = 125;
    /**
     * 最大副本上限
     */
    private static final short MAX_REPLICA_FACTOR = 2;
    /**
     * topic迁移过滤白名单
     */
    private static final List<String> WHITE_LIST_TOPICS = Collections.singletonList("qbus_simulator");

    public static void main(String[] args) throws Exception {
        // 配置源Kafka集群
        Properties sourceProps = new Properties();
        sourceProps.put("bootstrap.servers", "qbus14.add.bjmd.qihoo.net:9092");
        sourceProps.put("key.serializer", Serdes.String().serializer().getClass().getName());
        sourceProps.put("value.serializer", Serdes.String().serializer().getClass().getName());

        // 配置目标Kafka集群
        Properties targetProps = new Properties();
        targetProps.put("bootstrap.servers", "app05.cloud.bjyt.qihoo.net:39092");
        targetProps.put("key.serializer", Serdes.String().serializer().getClass().getName());
        targetProps.put("value.serializer", Serdes.String().serializer().getClass().getName());

        // 创建源Kafka集群的AdminClient
        try (AdminClient sourceAdminClient = AdminClient.create(sourceProps)) {
            // 获取源Kafka集群的所有topic详细信息
            Set<String> sourceTopics = sourceAdminClient.listTopics().names().get();
            Map<String, KafkaFuture<TopicDescription>> sourceTopicInfoMap = sourceAdminClient.describeTopics(sourceTopics).values();
            try (AdminClient targetAdminClient = AdminClient.create(targetProps)) {
                sourceTopicInfoMap.forEach((topicName, infoFuture) -> {
                    if (WHITE_LIST_TOPICS.contains(topicName)) {
                        return;
                    }
                    try {
                        CreateTopicsResult createResult = targetAdminClient.createTopics(Collections.singletonList(convertTopicMeta(topicName, infoFuture.get())));
                        Map<String, KafkaFuture<Void>> res = createResult.values();
                        res.values().forEach(a -> {
                            try {
                                a.get();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            } catch (ExecutionException e) {
                                System.out.println("topic创建失败" + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        System.out.println("获取topic详情失败：" + e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("所有topic已在目标Kafka集群中创建。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NewTopic convertTopicMeta(String name, TopicDescription topicInfo) {
        int partition = topicInfo.partitions().size();
        if (partition > MAX_NUM_PARTITIONS) {
            partition = MAX_NUM_PARTITIONS;
        }
        if (partition > 0) {
            //副本数
            short replicaFactor = (short) topicInfo.partitions().get(0).replicas().size();
            if (replicaFactor > MAX_REPLICA_FACTOR) {
                replicaFactor = MAX_REPLICA_FACTOR;
            }
            return new NewTopic(name, partition, replicaFactor);
        }
        return null;
    }
}