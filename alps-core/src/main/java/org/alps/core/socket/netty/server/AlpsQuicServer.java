package org.alps.core.socket.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.incubator.codec.quic.*;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsConfig;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.common.AlpsException;
import org.alps.core.common.AlpsSocketException;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * quic server
 */
@Slf4j
public class AlpsQuicServer extends AbstractAlpsServer {

    private final EventLoopGroup bossGroup;
    private final NettyServerConfig serverConfig;
    private final QuicServerConfig quicServerConfig;

    private Bootstrap bootstrap;
    private ChannelFuture serverFeature;

    public AlpsQuicServer(EventLoopGroup bossGroup, EventLoopGroup defaultGroup, NettyServerConfig serverConfig,
                          QuicServerConfig quicServerConfig, EnhancedSessionFactory sessionFactory,
                          List<AlpsConfig.ModuleConfig> supportModules, AlpsDataCoderFactory coderFactory) {
        super(serverConfig, coderFactory, defaultGroup, sessionFactory, supportModules);
        this.bossGroup = bossGroup;
        this.serverConfig = serverConfig;
        this.quicServerConfig = quicServerConfig;
    }

    @Override
    public void start() {
        var quicSslContext = QuicSslContextBuilder.forServer(quicServerConfig.getKey(), quicServerConfig.getKeyPassword(), quicServerConfig.getCertChain().toArray(new X509Certificate[0])).applicationProtocols("http/0.9").build();
        var quicServerCodecBuilder = new QuicServerCodecBuilder()
                .sslContext(quicSslContext)
                .initialMaxData(10000000).initialMaxStreamDataBidirectionalLocal(1000000)
                .initialMaxStreamDataBidirectionalRemote(1000000)
                .initialMaxStreamsBidirectional(100)
                .initialMaxStreamsUnidirectional(100);
        if (serverConfig.getTimeout() != null) {
            quicServerCodecBuilder.maxIdleTimeout(serverConfig.getTimeout().allIdleTime, TimeUnit.MILLISECONDS);
        }
        var channelHandler = quicServerCodecBuilder
                .tokenHandler(InsecureQuicTokenHandler.INSTANCE).handler(new ChannelInboundHandlerAdapter() {
                    @Override
                    public boolean isSharable() {
                        return true;
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        var channel = (QuicChannel) ctx.channel();
                        log.info("Connect {}", channel.remoteAddress());
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        ((QuicChannel) ctx.channel()).collectStats().addListener(future -> {
                            if (future.isSuccess()) {
                                log.info("Connect close {}", future.getNow());
                            }
                        });
                    }
                }).streamHandler(new ChannelInitializer<QuicStreamChannel>() {
                    @Override
                    protected void initChannel(QuicStreamChannel ch) throws Exception {
                        handleChannel(ch);
                    }
                }).build();
        this.bootstrap = new Bootstrap();
        try {
            this.serverFeature = bootstrap.group(bossGroup).channel(NioDatagramChannel.class).handler(channelHandler).bind(new InetSocketAddress(serverConfig.getPort())).sync();
        } catch (InterruptedException e) {
            throw new AlpsSocketException(e);
        }
        log.info("Server start finish. Listening port: {}", serverConfig.getPort());
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            bossGroup.shutdownGracefully();
            defaultGroup.shutdownGracefully();
            //对关闭通道进行监听
            try {
                this.serverFeature.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new AlpsException(e);
            }
        });
    }
}
