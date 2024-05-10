package com.example.mq_kafka.agg;

import lombok.Builder;
import lombok.SneakyThrows;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AggTopics {
    private static String businessPath = "/Users/sanyue/code/open/study-parent/mq/mq_kafka/src/main/resources/aggFile/business.txt";
    private static String topicInputPath = "/Users/sanyue/code/open/study-parent/mq/mq_kafka/src/main/resources/aggFile/topics_input.txt";
    private static String topicOutputPath = "/Users/sanyue/code/open/study-parent/mq/mq_kafka/src/main/resources/aggFile/topics_ouput.txt";
    private static final String resultPathPrefix = "/Users/sanyue/code/open/study-parent/mq/mq_kafka/src/main/resources/aggFile/";

    private static String cluster = "zzzc_priv_search_platform";

    private static Map<String, Map<String, Business>> businessMap = new HashMap<>();
    private static Map<String, TopicFlow> topicInputFlowMap = new HashMap<>();
    private static Map<String, TopicFlow> topicOutputFlowMap = new HashMap<>();
    private static Map<String, BusinessAndFlow> businessAndFlowMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        processData();
    }

    private static void processData() throws IOException {
        readBusiness();
        readInput();
        readOutput();
        aggData();
        processData(false);
    }

    private static void readBusiness() throws IOException {
        boolean firstLine = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(businessPath));
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] split = line.split("\t");
                String topicName = split[0];
                String business1 = split[1];
                String business2 = split[2];
                String owner = split[3];
                String usage = split[5];
                String cluster = split[6];

                if (businessMap.get(cluster) == null) {
                    businessMap.put(cluster, new HashMap<>());
                }
                businessMap.get(cluster).put(topicName, Business.builder().business1(business1).business2(business2).owner(owner).cluster(cluster).usage(usage).topicName(topicName).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void readInput() throws IOException {
        boolean firstLine = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(topicInputPath));
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] split = line.split("\t");
                String topicName = split[0].replace("{topic=\"", "").replace("\"}", "");
                String max = split[1];

                topicInputFlowMap.put(topicName, TopicFlow.builder().topicName(topicName).flow(max).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void readOutput() throws IOException {
        boolean firstLine = true;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(topicOutputPath));
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] split = line.split("\t");
                String topicName = split[0].replace("{topic=\"", "").replace("\"}", "");
                String max = split[1];

                topicOutputFlowMap.put(topicName, TopicFlow.builder().topicName(topicName).flow(max).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void aggData() {
        initResultMap();
        aggInput();
        aggOutput();
        aggBusiness();
    }

    private static void initResultMap() {
        businessAndFlowMap = new HashMap<>();
    }

    private static void aggInput() {
        for (Map.Entry<String, TopicFlow> topicFlowEntry : topicInputFlowMap.entrySet()) {
            String topicName = topicFlowEntry.getKey();
            TopicFlow topicFlow = topicFlowEntry.getValue();
            Business business = businessMap.get(cluster).get(topicName);

            if (businessAndFlowMap.get(topicName) == null) {
                BusinessAndFlow.BusinessAndFlowBuilder builder = BusinessAndFlow.builder();
                builder.topicName(topicName);
                builder.input(topicFlow.flow);
                if (business != null) {
                    builder.business1(business.business1);
                    builder.business2(business.business2);
                    builder.cluster(business.cluster);
                    builder.owner(business.owner);
                    builder.usage(business.usage);
                }
                businessAndFlowMap.put(topicName, builder.build());
            } else {
                businessAndFlowMap.get(topicName).input = topicFlow.flow;
            }
        }
    }

    private static void aggOutput() {
        for (Map.Entry<String, TopicFlow> topicFlowEntry : topicOutputFlowMap.entrySet()) {
            String topicName = topicFlowEntry.getKey();
            TopicFlow topicFlow = topicFlowEntry.getValue();
            Business business = businessMap.get(cluster).get(topicName);

            if (businessAndFlowMap.get(topicName) == null) {
                BusinessAndFlow.BusinessAndFlowBuilder builder = BusinessAndFlow.builder();
                builder.topicName(topicName);
                builder.output(topicFlow.flow);
                if (business != null) {
                    builder.business1(business.business1);
                    builder.business2(business.business2);
                    builder.cluster(business.cluster);
                    builder.owner(business.owner);
                    builder.usage(business.usage);
                }
                businessAndFlowMap.put(topicName, builder.build());
            } else {
                businessAndFlowMap.get(topicName).output = topicFlow.flow;
            }
        }
    }

    private static void aggBusiness() {
        for (Map.Entry<String, Business> businessEntry : businessMap.get(cluster).entrySet()) {
            Business business = businessEntry.getValue();
            if (businessAndFlowMap.get(business.topicName) == null) {
                BusinessAndFlow.BusinessAndFlowBuilder builder = BusinessAndFlow.builder().topicName(business.topicName).business1(business.business1).business2(business.business2).cluster(business.cluster).owner(business.owner).usage(business.usage);
                businessAndFlowMap.put(business.topicName, builder.build());
            }
        }
    }

    @SneakyThrows
    private static void processData(boolean filterEmptyFlow) {
        File file = new File(resultPathPrefix + cluster + ".txt");
        if (file.exists() && !file.delete()) {
            throw new Exception("delete file failed");
        }
        if (!file.createNewFile()) {
            throw new Exception("create file failed");
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write("Topic\t使用方式\t近7天最大写入流量\t近7天最大读取流量\t主部门\t副部门\t业务负责人\t集群\t");
            bw.newLine();

            for (Map.Entry<String, BusinessAndFlow> stringBusinessAndFlowEntry : businessAndFlowMap.entrySet()) {
                BusinessAndFlow businessAndFlow = stringBusinessAndFlowEntry.getValue();
                if (filterEmptyFlow) {
                    if (("0.00 B".equals(businessAndFlow.input) || "0 B".equals(businessAndFlow.input)) && ("0.00 B".equals(businessAndFlow.output) || "0 B".equals(businessAndFlow.output))) {
                        continue;
                    }
                }

                bw.write(nullToEmpty(businessAndFlow.topicName) + "\t" + nullToEmpty(businessAndFlow.usage) + "\t" + nullToEmpty(businessAndFlow.input) + "\t" + nullToEmpty(businessAndFlow.output) + "\t" + nullToEmpty(businessAndFlow.business1) + "\t" + nullToEmpty(businessAndFlow.business2) + "\t" + nullToEmpty(businessAndFlow.owner) + "\t" + nullToEmpty(businessAndFlow.cluster));
                bw.newLine();
            }
            bw.flush();
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    private static String nullToEmpty(String str) {
        return str == null || str.isEmpty() ? "-" : str;
    }

    @Builder
    private static class Business {
        public String topicName;
        public String business1;
        public String business2;
        public String cluster;
        public String owner;
        public String usage;
    }

    @Builder
    private static class TopicFlow {
        public String topicName;
        public String flow;
    }

    @Builder
    private static class BusinessAndFlow {
        public String topicName;
        public String business1;
        public String business2;
        public String cluster;
        public String owner;
        public String usage;
        public String input;
        public String output;
    }
}

