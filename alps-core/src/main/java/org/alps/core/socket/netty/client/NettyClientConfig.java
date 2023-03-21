package org.alps.core.socket.netty.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class NettyClientConfig {

    private String host;
    private int port;

    private Timeout timeout;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Timeout {
        long readerIdleTime;
        long writerIdleTime;
        long allIdleTime;
    }
}
