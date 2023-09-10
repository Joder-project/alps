package org.alps.core.socket.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.AlpsServer;
import org.alps.core.AlpsSession;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.socket.netty.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAlpsServer implements AlpsServer {

    final NettyServerConfig serverConfig;
    final AlpsDataCoderFactory coderFactory;
    final SocketConnectionHandle socketConnectionHandle;
    final AlpsMessageEncoder encoder;
    final AlpsServerProtocolHandler protocolHandler;
    final EnhancedSessionFactory sessionFactory;
    final List<AlpsSession> sessions = new CopyOnWriteArrayList<>();
    final List<String> supportModules;

    protected AbstractAlpsServer(NettyServerConfig serverConfig, AlpsDataCoderFactory coderFactory,
                                 EnhancedSessionFactory sessionFactory,
                                 List<String> supportModules) {
        this.serverConfig = serverConfig;
        this.coderFactory = coderFactory;
        this.socketConnectionHandle = new SocketConnectionHandle();
        this.encoder = new AlpsMessageEncoder();
        this.protocolHandler = new AlpsServerProtocolHandler(this, sessionFactory, supportModules);
        this.supportModules = Collections.unmodifiableList(supportModules);
        this.sessionFactory = sessionFactory;
    }

    protected void handleChannel(Channel socketChannel) {
        socketChannel.pipeline().addLast(
                new AlpsNettyEncoder(),
                new AlpsNettyDecoder(),
                new ProtobufDecoder(AlpsProtocol.AlpsPacket.getDefaultInstance()),
                encoder,
                new AlpsMessageDecoder(coderFactory)
        );
        if (serverConfig.getTimeout() != null) {
            NettyServerConfig.Timeout timeout = serverConfig.getTimeout();
            socketChannel.pipeline().addLast(new IdleStateHandler(
                    timeout.getReaderIdleTime(),
                    timeout.getWriterIdleTime(),
                    timeout.getAllIdleTime(),
                    TimeUnit.MILLISECONDS));
            socketChannel.pipeline().addLast(socketConnectionHandle);
        }
        //给pipeline管道设置处理器
        socketChannel.pipeline().addLast("server-handler", protocolHandler);
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
