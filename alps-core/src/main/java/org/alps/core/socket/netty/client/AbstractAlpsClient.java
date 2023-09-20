package org.alps.core.socket.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.alps.core.*;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.socket.netty.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public abstract class AbstractAlpsClient implements AlpsClient, IAlpsClientReady {
    final CopyOnWriteArrayList<AlpsSession> sessions = new CopyOnWriteArrayList<>();
    final AlpsMessageEncoder encoder;
    final AlpsClientProtocolHandler protocolHandler;
    final SocketConnectionHandle socketConnectionHandle;
    final AlpsDataCoderFactory coderFactory;
    final CountDownLatch isStart;
    final NettyClientConfig clientConfig;

    AbstractAlpsClient(NettyClientConfig clientConfig, EnhancedSessionFactory sessionFactory,
                       List<String> supportModules, AlpsDataCoderFactory coderFactory) {
        this.coderFactory = coderFactory;
        this.encoder = new AlpsMessageEncoder();
        this.protocolHandler = new AlpsClientProtocolHandler(this, sessionFactory, supportModules);
        this.socketConnectionHandle = new SocketConnectionHandle();
        this.isStart = new CountDownLatch(1);
        this.clientConfig = clientConfig;
    }

    protected void handleChannel(Channel socketChannel) {
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

    @Override
    public void ready() {
        this.isStart.countDown();
    }

    @Override
    public boolean isNotReady() {
        return isStart.getCount() > 0;
    }

    @Override
    public List<AlpsSession> session() {
        while (isNotReady()) {
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
