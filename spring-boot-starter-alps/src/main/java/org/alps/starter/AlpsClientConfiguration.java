package org.alps.starter;

import io.netty.channel.nio.NioEventLoopGroup;
import org.alps.core.AlpsClient;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.socket.netty.client.NettyAlpsClient;
import org.alps.core.socket.netty.client.NettyClientConfig;
import org.alps.starter.config.AlpsClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AlpsClientProperties.class, AlpsProperties.class})
public class AlpsClientConfiguration {

    @Bean(initMethod = "start", destroyMethod = "close")
    AlpsClient nettyAlpsClient(AlpsClientProperties clientProperties, AlpsProperties properties, EnhancedSessionFactory sessionFactory) {
        var nettyConfig = clientProperties.getNetty();
        var nettyClientConfig = new NettyClientConfig();
        var timeout = clientProperties.getTimeout();
        nettyClientConfig.setHost(clientProperties.getHost());
        nettyClientConfig.setPort(clientProperties.getPort());
        nettyClientConfig.setTimeout(new NettyClientConfig.Timeout(timeout.getReaderIdleTime(), timeout.getWriterIdleTime(), timeout.getAllIdleTime()));
        return new NettyAlpsClient(
                new NioEventLoopGroup(nettyConfig.getBossThread()),
                nettyClientConfig, sessionFactory,
                properties.getModules().stream().map(AlpsProperties.ModuleProperties::getCode).toList()
        );
    }

    @Bean
    ClientSessionManager clientSessionManager(AlpsClient client, AlpsProperties properties) {
        return new ClientSessionManager(client, properties);
    }
}
