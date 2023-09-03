package org.alps.core.socket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsEnhancedSession;

import java.net.SocketAddress;
import java.util.Map;

/**
 * 处理连接工具类,
 * copy from rocketmq
 */
@Slf4j
public class RemotingHelper {

    public static final AttributeKey<Map<String, AlpsEnhancedSession>> KEY = AttributeKey.valueOf("alps.session");

    public static String parseChannelRemoteAddr(final Channel channel) {
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (!addr.isEmpty()) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    public static void closeChannel(Channel channel) {
        final String addrRemote = RemotingHelper.parseChannelRemoteAddr(channel);
        var map = channel.attr(KEY).get();
        if (addrRemote.isEmpty()) {
            channel.close();
        } else {
            channel.close().addListener((ChannelFutureListener) future -> {
                log.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
                        future.isSuccess());
                if (map != null) {
                    map.forEach((k, v) -> v.close());
                }
            });
        }
    }
}
