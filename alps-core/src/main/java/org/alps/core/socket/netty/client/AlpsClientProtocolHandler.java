package org.alps.core.socket.netty.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsClient;
import org.alps.core.AlpsPacket;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.socket.netty.NettyAlpsSession;
import org.alps.core.socket.netty.RemotingHelper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class AlpsClientProtocolHandler extends SimpleChannelInboundHandler<AlpsPacket> {


    private final AlpsClient client;
    private final EnhancedSessionFactory sessionFactory;

    private final List<Short> supportModules;

    public AlpsClientProtocolHandler(AlpsClient client, EnhancedSessionFactory sessionFactory, List<Short> supportModules) {
        this.client = client;
        this.sessionFactory = sessionFactory;
        this.supportModules = supportModules;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("register channel, info: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("unregister channel, info: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("active channel, info: {}", ctx.channel().remoteAddress());
        // 初始化所有模块对象连接
        var map = ctx.channel().attr(RemotingHelper.KEY).get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            ctx.channel().attr(RemotingHelper.KEY).set(map);
        }
        map.computeIfAbsent(AlpsPacket.ZERO_MODULE, key -> {
            var sess = sessionFactory.create(new NettyAlpsSession(client, AlpsPacket.ZERO_MODULE, ctx.channel()));
            this.client.addSession(sess);
            return sess;
        });
        for (Short module : supportModules) {
            map.computeIfAbsent(module, key -> {
                var sess = sessionFactory.create(new NettyAlpsSession(client, module, ctx.channel()));
                this.client.addSession(sess);
                return sess;
            });
        }
        ((IAlpsClientReady) this.client).ready();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("Register an inactive channel");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AlpsPacket msg) throws Exception {
        var map = ctx.channel().attr(RemotingHelper.KEY).get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            ctx.channel().attr(RemotingHelper.KEY).set(map);
        }
        var session = map.computeIfAbsent(msg.module(), key -> {
            var sess = sessionFactory.create(new NettyAlpsSession(client, msg.module(), ctx.channel()));
            this.client.addSession(sess);
            return sess;
        });
        session.receive(msg);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.debug("add client! client info: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.debug("remove client! client info: {}", ctx.channel().remoteAddress());
        RemotingHelper.closeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("thrown an error! ", cause);
        RemotingHelper.closeChannel(ctx.channel());
    }
}
