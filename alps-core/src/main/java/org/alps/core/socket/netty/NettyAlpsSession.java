package org.alps.core.socket.netty;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.alps.core.AlpsPacket;
import org.alps.core.AlpsSession;
import org.alps.core.AlpsSocket;
import org.alps.core.proto.AlpsProtocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class NettyAlpsSession implements AlpsSession {

    private final AlpsSocket socket;
    private final Channel socketChannel;

    private final short module;

    private volatile boolean closeState;

    public NettyAlpsSession(AlpsSocket socket, short module, Channel socketChannel) {
        this.socket = socket;
        this.socketChannel = socketChannel;
        this.module = module;
        this.closeState = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attr(String key) {
        return (T) socketChannel.attr(AttributeKey.valueOf(key)).get();
    }

    @Override
    public AlpsSession attr(String key, Object value) {
        socketChannel.attr(AttributeKey.valueOf(key)).set(value);
        return this;
    }

    @Override
    public short module() {
        return module;
    }

    @Override
    public InetAddress selfAddress() {
        return ((InetSocketAddress) socketChannel.localAddress()).getAddress();
    }

    @Override
    public InetAddress targetAddress() {
        return ((InetSocketAddress) socketChannel.remoteAddress()).getAddress();
    }

    @Override
    public void send(AlpsPacket msg) {
        socketChannel.writeAndFlush(msg);
    }

    @Override
    public void send(AlpsProtocol.AlpsPacket protocol) {
        socketChannel.writeAndFlush(protocol);
    }

    @Override
    public void close() {
        if (this.closeState) {
            return;
        }
        socket.removeSession(this);
        this.closeState = true;
    }

    @Override
    public boolean isClose() {
        return closeState;
    }
}
