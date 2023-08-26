package org.alps.starter.config;

import lombok.Data;
import org.alps.starter.AlpsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = AlpsProperties.PATH + ".client")
@Component
public class AlpsClientProperties {

    private String host = "127.0.0.1";
    private int port = 6195;

    private SocketType type = SocketType.TCP;

    private Timeout timeout = new Timeout(5000, 5000, 5000);

    private TcpClientConfig tcp = new TcpClientConfig();

    private QuicClientConfig quic = new QuicClientConfig();

    @Data
    public static class TcpClientConfig {
        private int bossThread = 1;
    }

    @Data
    public static class QuicClientConfig {
        private int bossThread = 1;
    }

}
