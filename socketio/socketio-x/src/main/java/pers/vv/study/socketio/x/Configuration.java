package pers.vv.study.socketio.x;

import lombok.Data;
import pers.vv.study.socketio.x.netty.NettyConfig;

@Data
public class Configuration {

    private String hostname;
    private int port;

    private NettyConfig nettyConfig;

    public Configuration() {
        init();
    }

    private void init() {
        port = 9090;
        nettyConfig = new NettyConfig();
    }
}
