package org.alps.starter;

import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import org.alps.core.AlpsServer;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.socket.netty.server.NettyAlpsServer;
import org.alps.core.socket.netty.server.NettyServerConfig;
import org.alps.starter.config.AlpsServerProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AlpsServerProperties.class, AlpsProperties.class})
@AutoConfigureAfter(AlpsConfiguration.class)
public class AlpsServerConfiguration {

    @Bean(initMethod = "start", destroyMethod = "close")
    AlpsServer nettyAlpsServer(AlpsServerProperties properties, AlpsProperties alpsProperties, EnhancedSessionFactory sessionFactory) {
        var nettyConfig = properties.getNetty();
        var serverConfig = new NettyServerConfig();
        serverConfig.setPort(properties.getPort());
        var timeout = properties.getTimeout();
        serverConfig.setTimeout(new NettyServerConfig.Timeout(timeout.getReaderIdleTime(), timeout.getWriterIdleTime(), timeout.getAllIdleTime()));
        Map<ChannelOption<?>, Object> optionSettings = new HashMap<>();
        nettyConfig.getOptionSettings().forEach((k, v) -> optionSettings.put(ChannelOption.valueOf(k), v));
        serverConfig.setOptionSettings(optionSettings);

        Map<ChannelOption<?>, Object> childOptionSettings = new HashMap<>();
        nettyConfig.getChildOptionSettings().forEach((k, v) -> childOptionSettings.put(ChannelOption.valueOf(k), v));
        serverConfig.setChildOptionSettings(childOptionSettings);
        return new NettyAlpsServer(
                new NioEventLoopGroup(nettyConfig.getBossThread()),
                new NioEventLoopGroup(nettyConfig.getWorkerThread()),
                new NioEventLoopGroup(nettyConfig.getBizThread()),
                serverConfig, sessionFactory,
                alpsProperties.getModules().stream().map(AlpsProperties.ModuleProperties::getCode).toList()
        );
    }

    @Bean
    ServerSessionManager serverSessionManager(AlpsServer server, AlpsProperties properties) {
        return new ServerSessionManager(server, properties);
    }
}
