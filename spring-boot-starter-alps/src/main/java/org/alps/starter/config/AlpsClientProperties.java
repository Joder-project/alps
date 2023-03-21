package org.alps.starter.config;

import lombok.Data;
import org.alps.starter.AlpsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(AlpsProperties.PATH + ".client")
@Component
public class AlpsClientProperties {

    private String host = "127.0.0.1";
    private int port = 6195;

    private Timeout timeout = new Timeout(5000, 5000, 5000);

    private NettyConfig netty = new NettyConfig();

    @Data
    public static class NettyConfig {
        private int bossThread;
    }
}
