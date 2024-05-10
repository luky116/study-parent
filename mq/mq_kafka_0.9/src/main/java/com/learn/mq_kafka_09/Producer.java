/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.learn.mq_kafka_09;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer extends Thread {

//    public static final String BROKERS = "10.174.228.104:39092";

    // shbt_priv_search_platform
    // http://k4760v.add.shbt.qihoo.net:8888/clusters/shbt_priv_search_platform/brokers
//    public static final String BROKERS = "10.202.4.84:39092";
//    public static final String TOPIC = "qbus_simulator";

    // shbt_pub_infra_2
    // http://k4698v.add.shbt.qihoo.net:8888/clusters/shbt_pub_infra_2
    public static final String BROKERS = "10.217.117.3:39092";
    //    public static final String BROKER_LIST = "qbus120.add.shbt.qihoo.net:9092";
    public static final String TOPIC = "device_event";


    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BROKERS);
        props.put("client.id", "DemoProducer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);

        int partition = 10;
        for (int j = 0; j < partition; j++) {
            ProducerRecord<Integer, String> record = new ProducerRecord<>(TOPIC, j, null, "TEST MESSAGE");
            producer.send(record, (recordMetadata, e) -> {
                if (e != null) {
                    System.out.println("发送消息异常！partition = " + recordMetadata.partition() + "，offset = " + recordMetadata.offset());
                    e.printStackTrace();
                } else if (recordMetadata != null) {
                    // topic 下可以有多个分区，每个分区的消费者维护一个 offset
                    System.out.println("消息发送成功：partition=" + recordMetadata.partition() + "，offset=" + recordMetadata.offset());
                }
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Thread.sleep(500000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
