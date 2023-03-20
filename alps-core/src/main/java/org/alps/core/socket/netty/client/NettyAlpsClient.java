package org.alps.core.socket.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsClient;
import org.alps.core.AlpsSession;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.socket.netty.AlpsNettyDecoder;
import org.alps.core.socket.netty.AlpsNettyEncoder;
import org.alps.core.socket.netty.AlpsNioSocketChannel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class NettyAlpsClient implements AlpsClient {

    private final CopyOnWriteArrayList<AlpsSession> sessions = new CopyOnWriteArrayList<>();
    private final NioEventLoopGroup eventExecutors;
    private final NettyClientConfig clientConfig;
    private final AlpsNettyEncoder encoder;

    private final EnhancedSessionFactory sessionFactory;

    private final List<Short> supportModules;

    private final AlpsClientProtocolHandler protocolHandler;

    private Bootstrap bootstrap;

    private AlpsNioSocketChannel socketChannel;
    private volatile boolean isStart;

    public NettyAlpsClient(NioEventLoopGroup eventExecutors, NettyClientConfig clientConfig, EnhancedSessionFactory sessionFactory,
                           List<Short> supportModules) {
        this.eventExecutors = eventExecutors;
        this.clientConfig = clientConfig;
        this.sessionFactory = sessionFactory;
        this.supportModules = Collections.unmodifiableList(supportModules);
        this.encoder = new AlpsNettyEncoder();
        this.protocolHandler = new AlpsClientProtocolHandler(NettyAlpsClient.this, sessionFactory, supportModules);
    }

    @Override
    public void start() {
        //创建bootstrap对象，配置参数
        this.bootstrap = new Bootstrap();
        //设置线程组
        bootstrap.group(eventExecutors)
                //设置客户端的通道实现类型
                .channel(AlpsNioSocketChannel.class)
                //使用匿名内部类初始化通道
                .handler(new ChannelInitializer<AlpsNioSocketChannel>() {
                    @Override
                    protected void initChannel(AlpsNioSocketChannel ch) throws Exception {
                        handleChannel(ch);
                    }
                });
        log.debug("Start client finish!");
        //连接服务端
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(clientConfig.getHost(), clientConfig.getPort()).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //对通道关闭进行监听
        socketChannel = ((AlpsNioSocketChannel) channelFuture.channel());

        log.info("Connect Server=> {}:{}", clientConfig.getHost(), clientConfig.getPort());

    }

    private void handleChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(
                encoder,
                new AlpsNettyDecoder()
        );

        //给pipeline管道设置处理器
        socketChannel.pipeline().addLast("client-handler", protocolHandler);
    }

    void ready() {
        this.isStart = true;
    }

    public boolean isReady() {
        return isStart;
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            eventExecutors.shutdownGracefully();
            try {
                socketChannel.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public List<AlpsSession> session() {
        return Collections.unmodifiableList(sessions);
    }

    @Override
    public void addSession(AlpsSession session) {
        sessions.add(Objects.requireNonNull(session));
    }

    @Override
    public void removeSession(AlpsSession session) {
        sessions.remove(session);
    }
}
