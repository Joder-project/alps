package org.alps.core.socket.netty;

import io.netty.channel.Channel;
import io.netty.incubator.codec.quic.QuicStreamAddress;
import io.netty.util.AttributeKey;
import org.alps.core.AlpsPacket;
import org.alps.core.AlpsSession;
import org.alps.core.AlpsSocket;
import org.alps.core.common.AlpsAuthException;
import org.alps.core.proto.AlpsProtocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class NettyAlpsSession implements AlpsSession {

    private final AlpsSocket socket;
    private final Channel socketChannel;

    private final String module;
    private final int version;

    private final long verifyToken;

    private volatile boolean closeState;
    private volatile boolean auth;

    public NettyAlpsSession(AlpsSocket socket, String module, Channel socketChannel) {
        this(socket, module, -1, 0L, socketChannel, true);
    }

    public NettyAlpsSession(AlpsSocket socket, String module, int version, long verifyToken, Channel socketChannel) {
        this(socket, module, version, verifyToken, socketChannel, false);
    }

    public NettyAlpsSession(AlpsSocket socket, String module, int version, long verifyToken, Channel socketChannel, boolean auth) {
        this.socket = socket;
        this.socketChannel = socketChannel;
        this.module = module;
        this.version = version;
        this.verifyToken = verifyToken;
        this.closeState = false;
        this.auth = auth;
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
        if (isAuth()) {
            socketChannel.writeAndFlush(msg);
        }
    }

    @Override
    public void send(AlpsProtocol.AlpsPacket protocol) {
        if (isAuth()) {
            socketChannel.writeAndFlush(protocol);
        }
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

    @Override
    public boolean isAuth() {
        return auth || this.version <= 0;
    }

    @Override
    public void auth(int version, long verifyToken) {
        if (isAuth()) {
            return;
        }
        if (version == this.version && verifyToken == this.verifyToken) {
            this.auth = true;
            return;
        }
        throw new AlpsAuthException("认证失败");
    }
}
