package org.alps.core.socket.netty;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.alps.core.socket.netty.server.AlpsNioServerSocketChannel;

import java.nio.channels.SocketChannel;

public class AlpsNioSocketChannel extends NioSocketChannel {

    public AlpsNioSocketChannel() {
        super();
    }

    public AlpsNioSocketChannel(AlpsNioServerSocketChannel alpsNioServerSocketChannel, SocketChannel ch) {
        super(alpsNioServerSocketChannel, ch);
    }
}
