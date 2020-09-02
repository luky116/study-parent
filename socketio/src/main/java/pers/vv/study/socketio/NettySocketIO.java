package pers.vv.study.socketio;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class NettySocketIO {

    Logger logger = LoggerFactory.getLogger(NettySocketIO.class);

    public static void main(String[] args) {
        new NettySocketIO().start();
    }

    public void start() {
        Configuration config = new Configuration();
        config.setPort(8888);
        config.getSocketConfig().setReuseAddress(true);
        SocketIOServer server = new SocketIOServer(config);
        addListener(server);
        server.start();
    }

    private void addListener(SocketIOServer server) {
        server.addConnectListener(client -> logger.info("connected: {}", client.getSessionId()));
        server.addDisconnectListener(client -> logger.info("disconnect: {}", client.getSessionId()));
        server.addPingListener(client -> logger.info("{} -- {}", LocalDateTime.now(), client.getSessionId()));
        server.addEventListener("message", JSON.class, (client, data, ackSender) -> {
            Object d = JSON.toJSON(data);
            if (d instanceof JSONObject) {
                JSONObject jsonData = (JSONObject) d;
            }
            logger.info(JSON.toJSONString(d));
            ackSender.sendAckData(d);
        });
    }

}
