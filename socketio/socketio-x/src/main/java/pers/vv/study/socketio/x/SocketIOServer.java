package pers.vv.study.socketio.x;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.vv.study.socketio.x.netty.NettyInternalServer;

public class SocketIOServer {

    private static final Logger log = LoggerFactory.getLogger(SocketIOServer.class);

    private final Configuration config;

    private final InternalServer internalServer;

    public SocketIOServer(Configuration config) {
        this.config = config;
        this.internalServer = new NettyInternalServer(config);
    }

    public void start() {
        internalServer.start();
    }
}
