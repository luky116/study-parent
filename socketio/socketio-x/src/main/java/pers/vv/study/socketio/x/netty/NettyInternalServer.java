package pers.vv.study.socketio.x.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.vv.study.socketio.x.Configuration;
import pers.vv.study.socketio.x.InternalServer;

import java.net.InetSocketAddress;

public class NettyInternalServer implements InternalServer {

    private static final Logger log = LoggerFactory.getLogger(NettyInternalServer.class);

    private final Configuration config;

    private final NettyConfig nettyConfig;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyInternalServer(Configuration config) {
        this.config = config;
        this.nettyConfig = config.getNettyConfig();
    }

    @Override
    public void start() {
        startAsync().syncUninterruptibly();
    }

    public Future<Void> startAsync() {
        log.info("Session store / pubsub factory used");
        initGroups();
        ServerBootstrap b = getServerBootstrap();
        applyConnectionOptions(b);
        return b.bind(getSocketAddress()).addListener((FutureListener<Void>) future -> {
            if (future.isSuccess()) {
                log.info("SocketIO server started at port: {}", config.getPort());
            } else {
                log.error("SocketIO server start failed at port: {}!", config.getPort());
            }
        });
    }

    protected void initGroups() {
        if (nettyConfig.isUseLinuxNativeEpoll()) {
            bossGroup = new EpollEventLoopGroup(nettyConfig.getBossThreads());
            workerGroup = new EpollEventLoopGroup(nettyConfig.getWorkerThreads());
        } else {
            bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
            workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreads());
        }
    }

    protected ServerBootstrap getServerBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(getServerChannel())
                .childHandler(getChannelHandler());
        return serverBootstrap;
    }

    protected Class<? extends ServerChannel> getServerChannel() {
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
        if (nettyConfig.isUseLinuxNativeEpoll()) {
            channelClass = EpollServerSocketChannel.class;
        }
        return channelClass;
    }

    protected ChannelHandler getChannelHandler() {
        return new SocketIOChannelInitializer();
    }

    protected void applyConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.childOption(ChannelOption.TCP_NODELAY, nettyConfig.isTcpNoDelay());
        if (nettyConfig.getTcpSendBufferSize() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, nettyConfig.getTcpSendBufferSize());
        }
        if (nettyConfig.getTcpReceiveBufferSize() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, nettyConfig.getTcpReceiveBufferSize());
            bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, getRecvByteBufAllocator(nettyConfig));
        }
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isTcpKeepAlive());
        bootstrap.childOption(ChannelOption.SO_LINGER, nettyConfig.getSoLinger());

        bootstrap.option(ChannelOption.SO_REUSEADDR, nettyConfig.isReuseAddress());
        bootstrap.option(ChannelOption.SO_BACKLOG, nettyConfig.getAcceptBackLog());
    }

    private RecvByteBufAllocator getRecvByteBufAllocator(NettyConfig nettyConfig) {
        return new FixedRecvByteBufAllocator(nettyConfig.getTcpReceiveBufferSize());
    }

    protected InetSocketAddress getSocketAddress() {
        InetSocketAddress addr = new InetSocketAddress(config.getPort());
        if (config.getHostname() != null) {
            addr = new InetSocketAddress(config.getHostname(), config.getPort());
        }
        return addr;
    }

    @Override
    public void close() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
    }
}
