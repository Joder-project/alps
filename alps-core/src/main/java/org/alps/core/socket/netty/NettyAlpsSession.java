package org.alps.core.socket.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.incubator.codec.quic.QuicStreamAddress;
import io.netty.util.AttributeKey;
import org.alps.core.AlpsPacket;
import org.alps.core.AlpsSession;
import org.alps.core.AlpsSocket;
import org.alps.core.proto.AlpsProtocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class NettyAlpsSession implements AlpsSession {

    private final AlpsSocket socket;
    private final Channel socketChannel;

    private final String module;

    private volatile boolean closeState;

    public NettyAlpsSession(AlpsSocket socket, String module, Channel socketChannel) {
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
    public String module() {
        return module;
    }

    @Override
    public int delayMs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> selfAddress() {
        if (socketChannel == null) {
            return Optional.empty();
        }
        if (socketChannel.localAddress() instanceof QuicStreamAddress quicStreamAddress) {
            return Optional.of(quicStreamAddress.streamId() + "");
        }
        return Optional.ofNullable((InetSocketAddress) socketChannel.localAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress);
    }

    @Override
    public Optional<String> targetAddress() {
        if (socketChannel == null) {
            return Optional.empty();
        }
        if (socketChannel.remoteAddress() instanceof QuicStreamAddress quicStreamAddress) {
            return Optional.of(quicStreamAddress.streamId() + "");
        }
        return Optional.ofNullable((InetSocketAddress) socketChannel.remoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress);
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
    public void send(byte[] data) {
        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(data));
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
