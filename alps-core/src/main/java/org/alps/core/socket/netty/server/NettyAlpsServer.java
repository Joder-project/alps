package org.alps.core.socket.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsServer;
import org.alps.core.AlpsSession;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.common.AlpsException;
import org.alps.core.socket.netty.AlpsNettyDecoder;
import org.alps.core.socket.netty.AlpsNettyEncoder;
import org.alps.core.socket.netty.RemotingHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyAlpsServer implements AlpsServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final NettyServerConfig serverConfig;

    private final EnhancedSessionFactory sessionFactory;

    private final SocketConnectionHandle socketConnectionHandle;
    private final AlpsNettyEncoder encoder;
    private final EventLoopGroup defaultGroup;

    private final List<AlpsSession> sessions = new CopyOnWriteArrayList<>();
    private final List<Short> supportModules;
    private final AlpsServerProtocolHandler protocolHandler;

    private ServerBootstrap bootstrap;
    private ChannelFuture serverFeature;

    public NettyAlpsServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, EventLoopGroup defaultGroup,
                           NettyServerConfig serverConfig, EnhancedSessionFactory sessionFactory,
                           List<Short> supportModules) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverConfig = serverConfig;
        this.sessionFactory = sessionFactory;
        this.supportModules = Collections.unmodifiableList(supportModules);
        this.socketConnectionHandle = new SocketConnectionHandle();
        this.encoder = new AlpsNettyEncoder();
        this.defaultGroup = defaultGroup;
        this.protocolHandler = new AlpsServerProtocolHandler(NettyAlpsServer.this, sessionFactory, supportModules);
    }

    @Override
    public void start() {
        //创建服务端的启动对象，设置参数
        this.bootstrap = new ServerBootstrap();
        //设置两个线程组boosGroup和workerGroup
        bootstrap.group(bossGroup, workerGroup)
                //设置服务端通道实现类型
                .channel(AlpsNioServerSocketChannel.class);
//                .channelFactory(AlpsNioServerSocketChannel::new);
        serverConfig.getOptionSettings().forEach((k, v) -> {
            //设置线程队列得到连接个数
            bootstrap.option((ChannelOption<Object>) k, v);
        });
        serverConfig.getChildOptionSettings().forEach((k, v) -> {
            //设置保持活动连接状态
            bootstrap.childOption((ChannelOption<Object>) k, v);
        });
        //使用匿名内部类的形式初始化通道对象
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                handleChannel(socketChannel);
            }
        });

        //绑定端口号，启动服务端
        try {
            this.serverFeature = bootstrap.bind(serverConfig.getPort()).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Server start finish. Listening port: {}", serverConfig.getPort());
        //对关闭通道进行监听
        try {
            this.serverFeature.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new AlpsException(e);
        }
    }

    private void handleChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(
                encoder,
                new AlpsNettyDecoder()
        );
        if (serverConfig.getTimeout() != null) {
            NettyServerConfig.Timeout timeout = serverConfig.getTimeout();
            socketChannel.pipeline().addLast(new IdleStateHandler(
                    timeout.getReaderIdleTime(),
                    timeout.getWriterIdleTime(),
                    timeout.getAllIdleTime(),
                    TimeUnit.MILLISECONDS));
            socketChannel.pipeline().addLast(defaultGroup, socketConnectionHandle);
        }
        //给pipeline管道设置处理器
        socketChannel.pipeline().addLast(defaultGroup, "server-handler", protocolHandler);
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            defaultGroup.shutdownGracefully();
        });
    }

    @Override
    public List<AlpsSession> sessions() {
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
                    RemotingHelper.closeChannel(ctx.channel());
                }
            }
            ctx.fireUserEventTriggered(evt);
        }
    }
}
