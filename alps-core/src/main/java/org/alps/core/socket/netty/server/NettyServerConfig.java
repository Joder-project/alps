package org.alps.core.socket.netty.server;

import io.netty.channel.ChannelOption;
import lombok.Data;

import java.util.Map;

@Data
public class NettyServerConfig {

    private int port;
    Timeout timeout;


    private Map<ChannelOption<?>, ?> optionSettings;
    private Map<ChannelOption<?>, ?> childOptionSettings;

    @Data
    public static class Timeout {
        long readerIdleTime;
        long writerIdleTime;
        long allIdleTime;
    }
}
