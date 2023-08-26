package org.alps.starter;

import io.netty.channel.nio.NioEventLoopGroup;
import org.alps.core.AlpsClient;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.socket.netty.client.AlpsQuicClient;
import org.alps.core.socket.netty.client.AlpsTcpClient;
import org.alps.core.socket.netty.client.NettyClientConfig;
import org.alps.starter.config.AlpsClientProperties;
import org.alps.starter.config.SocketType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AlpsClientProperties.class, AlpsProperties.class})
public class AlpsClientConfiguration {

    @Bean(initMethod = "start", destroyMethod = "close")
    AlpsClient alpsClient(AlpsClientProperties clientProperties, AlpsProperties properties,
                          EnhancedSessionFactory sessionFactory, AlpsDataCoderFactory coderFactory) {
        if (clientProperties.getType() == SocketType.QUIC) {
            return alpsQuicClient(clientProperties, properties, sessionFactory, coderFactory);
        }
        return alpsTcpClient(clientProperties, properties, sessionFactory, coderFactory);
    }

    AlpsClient alpsTcpClient(AlpsClientProperties clientProperties, AlpsProperties properties,
                             EnhancedSessionFactory sessionFactory, AlpsDataCoderFactory coderFactory) {
        var nettyConfig = clientProperties.getTcp();
        var nettyClientConfig = new NettyClientConfig();
        var timeout = clientProperties.getTimeout();
        nettyClientConfig.setHost(clientProperties.getHost());
        nettyClientConfig.setPort(clientProperties.getPort());
        nettyClientConfig.setTimeout(new NettyClientConfig.Timeout(timeout.getReaderIdleTime(), timeout.getWriterIdleTime(), timeout.getAllIdleTime()));
        return new AlpsTcpClient(
                new NioEventLoopGroup(nettyConfig.getBossThread()),
                nettyClientConfig, sessionFactory,
                properties.getModules().stream().map(AlpsProperties.ModuleProperties::getCode).toList(),
                coderFactory);
    }

    AlpsClient alpsQuicClient(AlpsClientProperties clientProperties, AlpsProperties properties,
                              EnhancedSessionFactory sessionFactory, AlpsDataCoderFactory coderFactory) {
        var nettyConfig = clientProperties.getQuic();
        var nettyClientConfig = new NettyClientConfig();
        var timeout = clientProperties.getTimeout();
        nettyClientConfig.setHost(clientProperties.getHost());
        nettyClientConfig.setPort(clientProperties.getPort());
        nettyClientConfig.setTimeout(new NettyClientConfig.Timeout(timeout.getReaderIdleTime(),
                timeout.getWriterIdleTime(), timeout.getAllIdleTime()));
        return new AlpsQuicClient(new NioEventLoopGroup(nettyConfig.getBossThread()), nettyClientConfig, sessionFactory,
                properties.getModules().stream().map(AlpsProperties.ModuleProperties::getCode).toList(), coderFactory);

    }

    @Bean
    ClientSessionManager clientSessionManager(AlpsClient client, AlpsProperties properties) {
        return new ClientSessionManager(client, properties);
    }
}
