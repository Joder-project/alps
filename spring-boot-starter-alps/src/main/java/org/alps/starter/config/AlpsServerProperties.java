package org.alps.starter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alps.starter.AlpsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(AlpsProperties.PATH + ".server")
@Component
@AllArgsConstructor
@NoArgsConstructor
public class AlpsServerProperties {

    private int port = 6195;
    private Timeout timeout = new Timeout(10000, 10000, 10000);

    private SocketType socketType = SocketType.NETTY;

    private NettyConfig netty = new NettyConfig();


    enum SocketType {
        NETTY
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NettyConfig {
        private int bossThread = 1;
        private int workerThread = Runtime.getRuntime().availableProcessors();
        private int bizThread = Runtime.getRuntime().availableProcessors();

        private Map<String, ?> optionSettings = new HashMap<>();
        private Map<String, ?> childOptionSettings = new HashMap<>();
    }


}
