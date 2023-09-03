package org.alps.starter;

import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.alps.core.AlpsConfig;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.AlpsServer;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.socket.netty.server.AlpsQuicServer;
import org.alps.core.socket.netty.server.AlpsTcpServer;
import org.alps.core.socket.netty.server.NettyServerConfig;
import org.alps.core.socket.netty.server.QuicServerConfig;
import org.alps.starter.config.AlpsServerProperties;
import org.alps.starter.config.SocketType;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AlpsServerProperties.class, AlpsProperties.class})
@AutoConfigureAfter(AlpsConfiguration.class)
public class AlpsServerConfiguration {

    @Bean(initMethod = "start", destroyMethod = "close")
    AlpsServer alpsServer(AlpsServerProperties properties, AlpsProperties alpsProperties, EnhancedSessionFactory sessionFactory, AlpsDataCoderFactory coderFactory) throws Exception {
        if (properties.getType() == SocketType.QUIC) {
            return alpsQuicServer(properties, alpsProperties, sessionFactory, coderFactory);
        }
        return alpsTcpServer(properties, alpsProperties, sessionFactory, coderFactory);
    }


    AlpsServer alpsTcpServer(AlpsServerProperties properties, AlpsProperties alpsProperties, EnhancedSessionFactory sessionFactory, AlpsDataCoderFactory coderFactory) {
        var nettyConfig = properties.getTcp();
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
        return new AlpsTcpServer(new NioEventLoopGroup(nettyConfig.getBossThread()),
                new NioEventLoopGroup(nettyConfig.getWorkerThread()),
                new NioEventLoopGroup(nettyConfig.getBizThread()), serverConfig,
                sessionFactory,
                alpsProperties.getModules()
                        .stream()
                        .map(e -> new AlpsConfig.ModuleConfig(e.getName(), e.getVersion(), e.getVerifyToken()))
                        .toList(), coderFactory);
    }

    AlpsServer alpsQuicServer(AlpsServerProperties properties, AlpsProperties alpsProperties, EnhancedSessionFactory sessionFactory, AlpsDataCoderFactory coderFactory) throws CertificateException {
        var nettyConfig = properties.getQuic();
        var serverConfig = new NettyServerConfig();
        serverConfig.setPort(properties.getPort());
        var timeout = properties.getTimeout();
        serverConfig.setTimeout(new NettyServerConfig.Timeout(timeout.getReaderIdleTime(), timeout.getWriterIdleTime(), timeout.getAllIdleTime()));
        Map<ChannelOption<?>, Object> optionSettings = new HashMap<>();

        serverConfig.setOptionSettings(optionSettings);

        Map<ChannelOption<?>, Object> childOptionSettings = new HashMap<>();

        serverConfig.setChildOptionSettings(childOptionSettings);

        // TODO 修改
        var certificate = new SelfSignedCertificate();
        var quicServerConfig = new QuicServerConfig(certificate.key(), null,
                Collections.singletonList(certificate.cert()));


        return new AlpsQuicServer(new NioEventLoopGroup(nettyConfig.getBossThread()),
                new NioEventLoopGroup(nettyConfig.getWorkerThread()), serverConfig, quicServerConfig,
                sessionFactory, alpsProperties.getModules()
                .stream()
                .map(e -> new AlpsConfig.ModuleConfig(e.getName(), e.getVersion(), e.getVerifyToken()))
                .toList(), coderFactory);
    }

    @Bean
    ServerSessionManager serverSessionManager(AlpsServer server, AlpsProperties properties) {
        return new ServerSessionManager(server, properties);
    }
}
