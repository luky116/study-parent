package pers.vv.study.socketio.x;

import org.junit.jupiter.api.Test;

class SocketIOServerTest {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setPort(8888);
        config.getNettyConfig().setReuseAddress(true);

        new SocketIOServer(config).start();
    }

    @Test
    void start() {
    }
}
