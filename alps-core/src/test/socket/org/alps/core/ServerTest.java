package org.alps.core;

import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.socket.netty.server.NettyAlpsServer;
import org.alps.core.socket.netty.server.NettyServerConfig;

import java.util.Map;

@Slf4j
public class ServerTest {

    public static void main(String[] args) {
        var nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setPort(6195);
        nettyServerConfig.setOptionSettings(Map.of(
                ChannelOption.SO_BACKLOG, 128
        ));
        nettyServerConfig.setChildOptionSettings(Map.of(
                ChannelOption.SO_KEEPALIVE, true
        ));
        var routerDispatcher = new RouterDispatcher();

        var enhancedSessionFactory = new ClientTest.DefaultEnhancedSessionFactory(routerDispatcher);
        var server = new NettyAlpsServer(new NioEventLoopGroup(1),
                new NioEventLoopGroup(12),
                new NioEventLoopGroup(12),
                nettyServerConfig, enhancedSessionFactory,
                enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList());
        server.start();
    }
}
