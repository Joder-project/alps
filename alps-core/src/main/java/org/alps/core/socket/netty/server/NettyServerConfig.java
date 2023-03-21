package org.alps.core.socket.netty.server;

import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
public class NettyServerConfig {

    private int port;
    Timeout timeout;


    private Map<ChannelOption<?>, ?> optionSettings;
    private Map<ChannelOption<?>, ?> childOptionSettings;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Timeout {
        long readerIdleTime;
        long writerIdleTime;
        long allIdleTime;
    }
}
