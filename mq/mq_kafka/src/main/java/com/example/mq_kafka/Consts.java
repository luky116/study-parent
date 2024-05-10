package com.example.mq_kafka;

public class Consts {

//    public static final String TOPIC = "ap_feature_log";
//    public static final String TOPIC = "logsget_qdas_web_cuishou_pro";

    public static final String ConsumerOffsetTopic = "__consumer_offsets";
//    public static final String TOPIC = "qbus_simulator";

    // doc: https://geelib.qihoo.net/geelib/project/knowledge#subId=795&knowledgeId=379&docId=119649
    // 北京备用集群
    public static final String BROKER_LIST = "kafka28-1.add.lycc.qihoo.net:9092";
    public static final String TOPIC = "qbus_simulator";
    public static final String TOPIC2 = "qbus_simulator";
//    public static final String BROKER_LIST = "10.220.192.60:39092";
//    public static final String BROKER_LIST = "10.202.209.184:39092";

    // bjzdt_pub_infra_real_282
    // http://p89579v.hulk.bjzdt.qihoo.net:8888/clusters/bjzdt_pub_infra_real_282
//    public static final String BROKER_LIST = "10.228.134.100:39092";
//    public static final String TOPIC = "qbus_simulator";
//    public static final String TOPIC2 = "qbus_simulator";

    // 洛阳 2.8.2 测试集群
    // http://p11490v.hulk.bjyt.qihoo.net:8888/clusters/lycc_test_kafka28/topics
    // nginx 地址：https://hulk.qihoo.net/user/web/config
//    public static final String BROKER_LIST = "10.160.228.123:39092";
//    public static final String BROKER_LIST = "kafka28-1.add.lycc.qihoo.net:9052";
//    public static final String TOPIC = "test_new_topic_p1_r1";
//    public static final String TOPIC2 = "test_new_topic_p1_r1";
//    // nginx IP
//    public static final String BROKER_LIST = "p14157v.hulk.lycc.qihoo.net:9052";

    // nginx VIP方案
    // nginx IP
//    public static final String BROKER_LIST = "p14157v.hulk.lycc.qihoo.net:9052,p14158v.hulk.lycc.qihoo.net:9052,p14159v.hulk.lycc.qihoo.net:9052";
//    public static final String BROKER_LIST = "kafka28-1.add.lycc.qihoo.net:9052";

    //    public static final String BROKER_LIST = "p14158v.hulk.lycc.qihoo.net:9052";
//    public static final String BROKER_LIST = "10.160.229.174:39092";
//    public static final String TOPIC = "test_topic_p20_r3";
//    public static final String TOPIC2 = "test_topic_p10_r3";

    // bjmd_priv_ywtzb
    // http://kafka-console.qihoo.net/cluster/69/cluster
    // nginx：
//    public static final String BROKER_LIST = "11.33.160.23:9092,11.33.160.32:9092,11.33.160.36:9092";
    //  VIP：
//    public static final String BROKER_LIST = "10.220.162.228:39092";
//    public static final String TOPIC = "qwerrrrrr";
//    public static final String TOPIC2 = "test_for_nainx";

//
    // bjmd_priv_jrgpt
//    public static final String BROKER_LIST = "10.220.192.93:39092";
//    public static final String TOPIC = "call_limit";
//    public static final String TOPIC = "coll_call_statistic_topic";

    // shbt_priv_search_platform
    // http://k4760v.add.shbt.qihoo.net:8888/clusters/shbt_priv_search_platform/brokers
//    public static final String BROKER_LIST = "10.202.4.84:39092";
//    public static final String TOPIC = "LogTest";
//    public static final String TOPIC2 = "LogTest";
//    public static final String TOPIC = "qbus_simulator";
//    public static final String TOPIC2 = "qbus_simulator";
//    public static final String BROKER_LIST = "qbus1.se.shbt.qihoo.net:9092";

    // shbt_pub_infra_2
    // http://k4698v.add.shbt.qihoo.net:8888/clusters/shbt_pub_infra_2
//    public static final String BROKER_LIST = "10.217.117.3:39092";
//    public static final String BROKER_LIST = "qbus120.add.shbt.qihoo.net:9092";

    // bjzdt_priv_jinrong_282
    // 走nginx的VIP
//    public static final String BROKER_LIST = "10.228.134.153:39092";
    // 不走nginx的VIP
//    public static final String BROKER_LIST = "10.228.132.15:39092";
//    public static final String TOPIC = "tencent_gl_topic_uat";
//    public static final String TOPIC2 = "tencent_gl_topic_uat";




    // 测试的 nginx 端口
//    public static final String BROKER_LIST = "nginx7v.kafka.shbt.qihoo.net:9093,nginx8v.kafka.shbt.qihoo.net:9093,nginx9v.kafka.shbt.qihoo.net:9093";
//    public static final String BROKER_LIST = "10.217.117.3:39093";
//    public static final String TOPIC = "yuecai_test";
//    public static final String TOPIC2 = "yuecai_test2";


    // bjyt_pub_infra
    // http://k6800v.add.bjyt.qihoo.net:8888/clusters/bjyt_pub_infra
//    public static final String BROKER_LIST = "10.208.255.64:39092";
//    public static final String TOPIC = "qbus_simulator";

//    bjyt_pub_infra_1
//    public static final String BROKER_LIST = "10.209.221.140:39092";
//    public static final String TOPIC = "message_size_test";


    // 文渊测试的集群
//    public static final String BROKER_LIST = "tobtest04v.ops.bjyt.qihoo.net:9092";
//    public static final String TOPIC = "我看";

    // 本地的环境
//    public static final String BROKER_LIST = "127.0.0.1:9095";
//    public static final String TOPIC = "test_new_topic-2";


    // bjyt_pub_infra_2
    // http://k6800v.add.bjyt.qihoo.net:8888/clusters/bjyt_pub_infra_2
//    public static final String BROKER_LIST = "10.209.221.109:39092";
//    public static final String TOPIC = "alpha_access_log_test";

    // 浏览器 2.8.2 集群
    // shbt_priv_pcbrowser_282
    // http://p10250v.hulk.shbt.qihoo.net:8888/clusters/shbt_priv_pcbrowser_282/topics/pc_browser_dd_test
//    public static final String BROKER_LIST = "10.204.196.64:39092";
//    public static final String BROKER_LIST = "kafka282-1.pcbrowser.shbt.qihoo.net:9092";
//    public static final String TOPIC = "pc_browser_dd_test";
//    public static final String TOPIC = "te1";

    // shbt_pubbak_infra 上海备用集群
    // https://geelib.qihoo.net/geelibv3/knowledgeOpen#knowledgeId=379&docId=119796
    // http://k4698v.add.shbt.qihoo.net:8888/clusters/shbt_pubbak_infra
//    public static final String BROKER_LIST = "10.204.196.100:39092";
//    public static final String BROKER_LIST = "bak-qbus02.add.shbt.qihoo.net:9092";
//    public static final String BROKER_LIST = "10.216.30.55:29091";
//    public static final String TOPIC = "perform_topic_p100_2";

    // nginx测试
    //
//    public static final String BROKER_LIST = "10.216.30.55:29091,10.216.30.55:29092,10.216.30.55:29093,10.216.30.55:29094";
//    public static final String TOPIC = "perform_topic_p100_2";
//    public static final String BROKER_LIST = "10.216.30.55:39093";
//    public static final String TOPIC = "perform_topic_p100_2";

    // 游戏私有集群
    // http://p10250v.hulk.shbt.qihoo.net:8888/clusters/shbt_priv_game/topics/qbus_simulator
    // https://geelib.qihoo.net/geelibv3/knowledgeOpen#knowledgeId=379&docId=128950
//    public static final String BROKER_LIST = "10.217.117.66:39092";
//    public static final String TOPIC = "qbus_simulator";

    // zzzc_pub_infra_1
    // http://k2137v.add.zzzc.qihoo.net:8888/clusters/zzzc_pub_infra_1
//    public static final String BROKER_LIST = "10.174.228.104:39092";
//    public static final String TOPIC = "testyuecai";
//    public static final String TOPIC2 = "qbus_simulator";

    // 游戏282集群
//    public static final String BROKER_LIST = "10.216.30.55:29091,10.216.30.55:29092,10.216.30.55:29093,10.216.30.55:29094";
//    public static final String TOPIC = "perform_topic_p200_2";

    // bjpdc_pub_infra
    // http://p54757v.hulk.bjzdt.qihoo.net:8888/clusters/bjpdc_pub_infra/topics/__consumer_offsets
//    public static final String BROKER_LIST = "10.224.144.18:39092";
//    public static final String TOPIC = "webcheck-log";

    // bjmd_priv_jinrong
//    public static final String BROKER_LIST = "10.220.162.10:39092";
//    public static final String TOPIC = "canal_lcs_test";

    // CDN 228 log 集群
    // bjzdt_pub_infra_log_282
    // Nginx：10.228.134.99:39092
//        public static final String BROKER_LIST = "kafka282-01.add.bjzdt.qihoo.net:9092";
//    public static final String BROKER_LIST = "10.228.134.99:39092";
//    public static final String TOPIC = "xinfra-monitor-topic";
//    public static final String TOPIC2 = "xinfra-monitor-topic";


}
