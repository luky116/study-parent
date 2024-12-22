package pers.vv.study.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class EtcdService {
    private final Client client;

    /**
     * 构造函数，初始化 ETCD 客户端
     *
     * @param endpoints ETCD 服务地址
     */
    public EtcdService(String endpoints) {
        this.client = Client.builder().endpoints(endpoints).build();
    }

    /**
     * 写入键值
     *
     * @param key   要写入的键
     * @param value 要写入的值
     */
    public void write(String key, String value) {
        ByteSequence keySeq = ByteSequence.from(key, StandardCharsets.UTF_8);
        ByteSequence valueSeq = ByteSequence.from(value, StandardCharsets.UTF_8);

        client.getKVClient().put(keySeq, valueSeq).thenAccept(putResponse -> {
            System.out.println("[WRITE] Key: " + key + ", Value: " + value);
        }).exceptionally(e -> {
            System.err.println("[WRITE FAILED] Key: " + key + ", Value: " + value);
            e.printStackTrace();
            throw new RuntimeException(e);
        });
    }

    /**
     * 读取键值
     *
     * @param key 要读取的键
     */
    public void read(String key) {
        ByteSequence keySeq = ByteSequence.from(key, StandardCharsets.UTF_8);

        client.getKVClient().get(keySeq).thenAccept(response -> {
            if (response.getKvs().isEmpty()) {
                System.out.println("[READ] Key not found: " + key);
            } else {
                String value = response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
                System.out.println("[READ] Key: " + key + ", Value: " + value);
            }
        }).exceptionally(e -> {
            System.err.println("[READ FAILED] Key: " + key);
            e.printStackTrace();
            throw new RuntimeException(e);
        });
    }

    /**
     * 删除键值
     *
     * @param key 要删除的键
     */
    public void delete(String key) {
        ByteSequence keySeq = ByteSequence.from(key, StandardCharsets.UTF_8);

        client.getKVClient().delete(keySeq).thenAccept(deleteResponse -> {
            System.out.println("[DELETE] Key: " + key + ", Deleted Count: " + deleteResponse.getDeleted());
        }).exceptionally(e -> {
            System.err.println("[DELETE FAILED] Key: " + key);
            e.printStackTrace();
            throw new RuntimeException(e);
        });
    }

    /**
     * 关闭客户端
     */
    public void close() {
        client.close();
        System.out.println("[CLOSE] ETCD client closed.");
    }
}