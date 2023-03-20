package org.alps.core.socket.netty;

import org.alps.core.AlpsPacket;
import org.alps.core.AlpsSession;
import org.alps.core.AlpsSocket;

import java.net.InetAddress;

public class NettyAlpsSession implements AlpsSession {

    private final AlpsSocket socket;
    private final AlpsNioSocketChannel socketChannel;

    private final short module;

    public NettyAlpsSession(AlpsSocket socket, short module, AlpsNioSocketChannel socketChannel) {
        this.socket = socket;
        this.socketChannel = socketChannel;
        this.module = module;
    }

    @Override
    public short module() {
        return module;
    }

    @Override
    public InetAddress selfAddress() {
        return socketChannel.localAddress().getAddress();
    }

    @Override
    public InetAddress targetAddress() {
        return socketChannel.remoteAddress().getAddress();
    }

    @Override
    public void send(AlpsPacket msg) {
        socketChannel.writeAndFlush(msg);
    }

    @Override
    public void close() {
        socket.removeSession(this);
    }
}
