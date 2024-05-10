package com.example.mq_kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Checker {
    static String cluster;
    static String brokerList;
    static String topic;
    static String phone;
    static String group_name;
    static String alarmMsg;
    static String ignorePartitions;
    static int alarmThreshold;
    static int delayThresholdMs;
    static int delayThresholdCnt;
    static String delayAlarmMsg;
    static String recoveryMsg;
    static Map<Integer, Integer> partitionDelayCntMap = new HashMap<Integer, Integer>();
    static Map<Integer, String> partitionNodeMap = new HashMap<Integer, String>();
    static Map<Integer, Boolean> partitionErrorMap = new HashMap<Integer, Boolean>();
    static String[] ignorePartitionArray = {};

    public static void main(String args[]) {
        Properties props = new Properties();
        try {
            InputStream in = Checker.class.getResourceAsStream("/config.properties");
            props.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cluster = props.getProperty("cluster");
        brokerList = props.getProperty("broker_list");
        topic = props.getProperty("topic");
        phone = props.getProperty("phone");
        group_name = props.getProperty("group_name");
        alarmMsg = props.getProperty("alarm_msg");
        alarmThreshold = Integer.parseInt(props.getProperty("alarm_threshold"));
        delayThresholdMs = Integer.parseInt(props.getProperty("delay_threshold_ms"));
        delayThresholdCnt = Integer.parseInt(props.getProperty("delay_threshold_cnt"));
        delayAlarmMsg = props.getProperty("delay_alarm_msg");
        recoveryMsg = props.getProperty("recovery_msg");
        ignorePartitions = props.getProperty("ignore_partitions");
        ignorePartitionArray = ignorePartitions.split(",");

        while (true) {
            produceCheck();
            System.out.println(new Date());
        }
    }

    public static void produceCheck() {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 0);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        int currentPartition = -1;
        try {
            List<PartitionInfo> partitionList = producer.partitionsFor(topic);

            for (PartitionInfo p : partitionList) {
                if (p.leader() == null) {
                    System.out.println("no leader:" + p.partition());
                    continue;
                }
                if (partitionList.size() > partitionNodeMap.size()) {
                    partitionNodeMap.put(p.partition(), p.leader().host());
                }
                if (partitionDelayCntMap.containsKey(p.partition()) == false) {
                    partitionDelayCntMap.put(p.partition(), 0);
                }
                if (partitionErrorMap.containsKey(p.partition()) == false) {
                    partitionErrorMap.put(p.partition(), false);
                }
            }

            for (PartitionInfo p : partitionList) {
                currentPartition = p.partition();
                System.out.println("current test partition:" + p.partition());
                if (Arrays.asList(ignorePartitionArray).contains(Integer.toString(currentPartition))) {
                    continue;
                }
                Instant start = Instant.now();
                producer.send(new ProducerRecord<String, String>(topic, currentPartition, null, "qbus checker message")).get();
                Instant end = Instant.now();

                Duration timeElapsed = Duration.between(start, end);
                System.out.println("Partition: " + currentPartition + ", Time taken: " + timeElapsed.toMillis() + " milliseconds");
                //System.out.println("delay cnt: " + partitionDelayCntMap.get(currentPartition));
                //System.out.println("error condition: " + partitionErrorMap.get(currentPartition));
                if (timeElapsed.toMillis() > delayThresholdMs) {
                    Integer cnt = partitionDelayCntMap.get(currentPartition);
                    cnt++;
                    if (cnt > delayThresholdCnt) {
                        sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + delayAlarmMsg + "(" + partitionNodeMap.get(currentPartition) + ")");
                        cnt = 0;

                        partitionErrorMap.put(currentPartition, true);
                    }
                    partitionDelayCntMap.put(currentPartition, cnt);
                } else {
                    partitionDelayCntMap.put(currentPartition, 0);

                    if (partitionErrorMap.get(currentPartition) == true) {
                        sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + recoveryMsg + "(" + partitionNodeMap.get(currentPartition) + ")");
                        partitionErrorMap.put(currentPartition, false);
                    }
                }
            }
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Partition: " + currentPartition + " " + e);
            if (currentPartition >= 0) {
                if (partitionNodeMap.get(currentPartition) != null) {
                    sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + alarmMsg + "(" + partitionNodeMap.get(currentPartition) + ")");
                    partitionErrorMap.put(currentPartition, true);
                } else {
                    sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + alarmMsg + "(partition number:" + currentPartition + ")");
                }

            }
            //else
            //      sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + "All brokers down!");
        } finally {
            producer.close();
        }
    }

    public static void consumeCheck() {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("group.id", "checker_group");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        try {
            List<PartitionInfo> availablePartitions = consumer.partitionsFor(topic);
            List<TopicPartition> partitions = new ArrayList<TopicPartition>();
            for (PartitionInfo p : availablePartitions) {
                TopicPartition partition = new TopicPartition(topic, p.partition());
                partitions.add(partition);
            }

            consumer.assign(partitions);

            ConsumerRecords<String, String> records = consumer.poll(2000);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record);
                consumer.commitSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", "15810569135", "This is a test");
        } finally {
            consumer.close();
        }
    }

    public static String EncoderByMd5(String str) {
        String newStr = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //md5.update(str.getBytes());
            byte[] digest = md5.digest(str.getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            newStr = bigInt.toString(16);
            for (int i = 0; i < 32 - newStr.length(); ++i) {
                newStr = "0" + newStr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return newStr;
    }

    public static void sendAlarm(String urls, String phoneNumbers, String alarmMsg) {
        try {
            String url = "http://api.wonder.corp.qihoo.net:80/sender/api/send-msg/send-duty";
            //String app_key = "infrastructure";
            //String app_secret_key = "6a8a37f1c14a3e904586909d";
            String parmate = "subject=" + URLEncoder.encode(alarmMsg, "utf-8") + "&group_name=" + URLEncoder.encode(phoneNumbers, "utf-8");
            //String sign = EncoderByMd5(EncoderByMd5(parmate) + app_secret_key);
            String getURL = url + "?" +
                    // "app_key=infrastructure" +
                    // "&sign=" + sign + "&" + parmate;
                    parmate;
            URL getUrl = new URL(getURL);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestProperty("hulksrc", "dba");
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String lines;
            while ((lines = reader.readLine()) != null) {
                System.out.println(lines);
            }
            reader.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}