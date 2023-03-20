package org.alps.core.socket.netty.server;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.socket.netty.AlpsNioSocketChannel;

import java.nio.channels.SocketChannel;
import java.util.List;

@Slf4j
public class AlpsNioServerSocketChannel extends NioServerSocketChannel {

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = SocketUtils.accept(javaChannel());

        try {
            if (ch != null) {
                buf.add(new AlpsNioSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            log.warn("Failed to create a new channel from an accepted socket.", t);

            try {
                ch.close();
            } catch (Throwable t2) {
                log.warn("Failed to close a socket.", t2);
            }
        }

        return 0;
    }
}
