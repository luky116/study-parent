package com.example.mq_kafka.alarm;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Checker09 {
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
    private static final DateTimeFormatter ZDT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String filePath = System.getProperty("user.dir") + "/alarm.log";

    /**
     *
     */
    public static void sendAlarmByYY(String urls, String phoneNumbers, String alarmMsg) {
        System.out.println("Alarm on" + filePath);
        writeFile(filePath, String.format("%s\t%s\t%s", ZDT_FORMATTER.format(ZonedDateTime.now()), phoneNumbers, alarmMsg));
    }

    /**
     *
     */
    public static void recoverAlarm() {
        System.out.println("Alarm off" + filePath);
        writeFile(filePath, "");
    }

    public static void main(String[] args) {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(Checker09::recoverAlarm, 0, 70, TimeUnit.SECONDS);

        Properties props = new Properties();
        try {
            InputStream in = Checker.class.getResourceAsStream("./config.properties");
            props.load(in);
            in.close();
        } catch (Exception e) {
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
                if (!partitionDelayCntMap.containsKey(p.partition())) {
                    partitionDelayCntMap.put(p.partition(), 0);
                }
                if (!partitionErrorMap.containsKey(p.partition())) {
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

                    if (partitionErrorMap.get(currentPartition)) {
                        sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + recoveryMsg + "(" + partitionNodeMap.get(currentPartition) + ")");
                        partitionErrorMap.put(currentPartition, false);
                    }
                }
            }
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Partition: " + currentPartition + " " + e);
            if (currentPartition >= 0) {
                if (partitionNodeMap.get(currentPartition) != null) {
                    sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + alarmMsg + "(" + partitionNodeMap.get(currentPartition) + ")");
                    sendAlarmByYY("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + alarmMsg + "(" + partitionNodeMap.get(currentPartition) + ")");
                    partitionErrorMap.put(currentPartition, true);
                } else {
                    sendAlarm("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + alarmMsg + "(partition number:" + currentPartition + ")");
                    sendAlarmByYY("http://alarms.ops.qihoo.net:8360/intfs/sms_intf", group_name, "[" + cluster + "]" + alarmMsg + "(partition number:" + currentPartition + ")");
                }

            }
        } finally {
            producer.close();
        }
    }


    public static void sendAlarm(String urls, String phoneNumbers, String alarmMsg) {
        try {
            String url = "http://api.wonder.qihoo.net:80/sender/api/send-msg/send-duty";
            String parmate = "subject=" + URLEncoder.encode(alarmMsg, "utf-8") + "&group_name=" + URLEncoder.encode(phoneNumbers, "utf-8");
            String getURL = url + "?" +
                    parmate;
            URL getUrl = new URL(getURL);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestProperty("hulksrc", "dba");
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
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

    public static void writeFile(String filePath, String content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath);
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}