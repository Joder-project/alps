package org.alps.core.socket.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.*;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.socket.netty.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyAlpsClient implements AlpsClient {

    private final CopyOnWriteArrayList<AlpsSession> sessions = new CopyOnWriteArrayList<>();
    private final NioEventLoopGroup eventExecutors;
    private final NettyClientConfig clientConfig;
    private final AlpsMessageEncoder encoder;


    private final AlpsClientProtocolHandler protocolHandler;
    private final SocketConnectionHandle socketConnectionHandle;

    private final AlpsDataCoderFactory coderFactory;

    private Bootstrap bootstrap;

    private AlpsNioSocketChannel socketChannel;
    private volatile boolean isStart;

    public NettyAlpsClient(NioEventLoopGroup eventExecutors, NettyClientConfig clientConfig, EnhancedSessionFactory sessionFactory,
                           List<Short> supportModules, AlpsDataCoderFactory coderFactory) {
        this.eventExecutors = eventExecutors;
        this.clientConfig = clientConfig;
        this.coderFactory = coderFactory;
        this.encoder = new AlpsMessageEncoder();
        this.protocolHandler = new AlpsClientProtocolHandler(NettyAlpsClient.this, sessionFactory, supportModules);
        this.socketConnectionHandle = new SocketConnectionHandle();
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
        while (!isReady()) {
        }
        log.info("Connect Server=> {}:{}", clientConfig.getHost(), clientConfig.getPort());

    }

    private void handleChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(
                new AlpsNettyEncoder(),
                new AlpsNettyDecoder(),
                new ProtobufDecoder(AlpsProtocol.AlpsPacket.getDefaultInstance()),
                encoder,
                new AlpsMessageDecoder(coderFactory)
        );
        if (clientConfig.getTimeout() != null) {
            var timeout = clientConfig.getTimeout();
            socketChannel.pipeline().addLast(new IdleStateHandler(
                    timeout.getReaderIdleTime(),
                    timeout.getWriterIdleTime(),
                    timeout.getAllIdleTime(),
                    TimeUnit.MILLISECONDS));
            socketChannel.pipeline().addLast(socketConnectionHandle);
        }
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
        while (!isReady()) {
        }
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

    @ChannelHandler.Sharable
    static class SocketConnectionHandle extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent event) {
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    var map = ctx.channel().attr(RemotingHelper.KEY).get();
                    if (map != null) {
                        var session = map.get(AlpsPacket.ZERO_MODULE);
                        if (session != null) {
                            session.idle().send();
                        }
                    }
                }
            }
            ctx.fireUserEventTriggered(evt);
        }
    }
}
