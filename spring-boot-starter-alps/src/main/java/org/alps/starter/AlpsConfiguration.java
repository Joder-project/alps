package org.alps.starter;

import org.alps.core.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AlpsProperties.class)
public class AlpsConfiguration {

    @Bean
    EnhancedSessionFactory sessionFactory(FrameCoders frameCoders, AlpsDataCoderFactory dataCoderFactory,
                                          FrameListeners frameListeners, AlpsProperties properties) {
        AlpsConfig config = new AlpsConfig(properties.isServer(), properties.getMetadataConfig(), properties.getDataConfig(),
                properties.getModules().stream()
                        .map(e -> new AlpsConfig.ModuleConfig(e.getCode(), e.getVersion(), e.getVerifyToken())).toList());
        return new DefaultEnhancedSessionFactory(frameCoders, dataCoderFactory, frameListeners, config);
    }

    @Bean
    AlpsDataCoderFactory dataCoderFactory() {
        return new AlpsDataCoderFactory();
    }

    @Bean
    FrameListeners frameListeners(RouterDispatcher routerDispatcher) {
        return new FrameListeners(routerDispatcher);
    }

    @Bean
    FrameCoders frameCoders(AlpsDataCoderFactory dataCoderFactory) {
        return new FrameCoders(dataCoderFactory);
    }

    @Bean
    RouterDispatcher routerDispatcher() {
        return new RouterDispatcher();
    }

    @Bean
    RouterBeanPostProcessor routerBeanPostProcessor(AlpsProperties properties, RouterDispatcher dispatcher) {
        return new RouterBeanPostProcessor(dispatcher, properties);
    }
}
