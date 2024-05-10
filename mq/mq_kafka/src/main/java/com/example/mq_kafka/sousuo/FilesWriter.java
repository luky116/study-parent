package com.example.mq_kafka.sousuo;


import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FilesWriter {
    private String filePath = "/Users/sanyue/code/open/study-parent/mq/mq_kafka/src/main/resources/data44.txt";
    private BufferedWriter writer;
    private AtomicInteger count;
    private int bufferSizeMB = 1;

    private long startTs;
    private long endTs;

    BlockingQueue<ConsumerRecords<String, String>> queue = new LinkedBlockingQueue<>(200);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FilesWriter() throws IOException, ParseException {
        File file = new File(filePath);
        if (!file.exists()) {
//            file.delete();
            file.createNewFile();
        }

        writer = new BufferedWriter(new FileWriter(filePath, true), 1024 * 1024 * bufferSizeMB);
        count = new AtomicInteger(0);


        startTs = formatter.parse("2024-05-08 14:0:00").getTime() / 1000;
        endTs = formatter.parse("2024-05-10 14:00:00").getTime() / 1000;

        // 异步 flush
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    synchronized (writer) {
                        writer.flush();
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            try {
                writeSync();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void write(ConsumerRecords<String, String> records) throws InterruptedException {
        queue.put(records);
    }

    public void writeSync() throws InterruptedException, IOException {
        while (true) {
            ConsumerRecords<String, String> records = queue.take();
            for (ConsumerRecord<String, String> record : records) {
                String line = filter(record);
                if (line.isEmpty()) {
                    continue;
                }
                writer.write(line);
            }
        }
    }
//    }

    private String filter(ConsumerRecord<String, String> record) {
        String line = record.value();
        if (line == null || line.trim().isEmpty()) {
            return "";
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(line);
        } catch (Exception e) {
            System.err.println("parse error: " + line);
            e.printStackTrace();
            return "";
        }

        long dlts = jsonObject.getLongValue("dlts");
        String uuid = jsonObject.getString("uuid");

        if (dlts < startTs || dlts > endTs) {
            return "";
        }

        return dlts + "\t" + uuid + "\t" + record.partition() + "\t" + formatter.format(new Date(dlts * 1000)) + "\n";
    }

}
