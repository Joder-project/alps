package org.alps.core.socket.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.incubator.codec.quic.*;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.common.AlpsSocketException;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AlpsQuicClient extends AbstractAlpsClient {

    private final NioEventLoopGroup eventExecutors;
    private Bootstrap bootstrap;
    private Channel channel;
    private QuicChannel quicChannel;
    private QuicStreamChannel quicStreamChannel;

    public AlpsQuicClient(NioEventLoopGroup eventExecutors, NettyClientConfig clientConfig, EnhancedSessionFactory sessionFactory,
                          List<String> supportModules, AlpsDataCoderFactory coderFactory) {
        super(clientConfig, sessionFactory, supportModules, coderFactory);
        this.eventExecutors = eventExecutors;
    }

    @Override
    public void start() {
        var quicSslContext = QuicSslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocols("http/0.9").build();

        var quicClientCodecBuilder = new QuicClientCodecBuilder()
                .sslContext(quicSslContext)
                .initialMaxData(100000)
                .initialMaxStreamDataBidirectionalLocal(100000);
        if (clientConfig.getTimeout() != null) {
            quicClientCodecBuilder.maxIdleTimeout(clientConfig.getTimeout().allIdleTime, TimeUnit.MILLISECONDS);
        }
        var coder = quicClientCodecBuilder.build();
        this.bootstrap = new Bootstrap();
        try {
            this.channel = bootstrap.group(eventExecutors)
                    .channel(NioDatagramChannel.class)
                    .handler(coder)
                    .bind(0).sync()
                    .channel();
            this.quicChannel = QuicChannel.newBootstrap(channel)
                    .streamHandler(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ctx.close();
                        }
                    }).remoteAddress(new InetSocketAddress(clientConfig.getHost(), clientConfig.getPort()))
                    .connect()
                    .get();
            this.quicStreamChannel = quicChannel.createStream(QuicStreamType.BIDIRECTIONAL, new ChannelInitializer<QuicStreamChannel>() {
                @Override
                protected void initChannel(QuicStreamChannel ch) throws Exception {
                    handleChannel(ch);
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if (evt == ChannelInputShutdownReadComplete.INSTANCE) {
                                ((QuicChannel) ctx.channel().parent()).close(true, 0,
                                        ctx.alloc().directBuffer(16)
                                                .writeBytes(new byte[]{'k', 't', 'h', 'x', 'b', 'y', 'e'}));
                            }
                        }
                    });
                }
            }).sync().getNow();
        } catch (Exception e) {
            throw new AlpsSocketException(e);
        }
        try {
            isStart.await();
            log.info("Connect Server=> {}:{}", clientConfig.getHost(), clientConfig.getPort());
        } catch (Exception e) {
            log.error("await error", e);
        }
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            quicChannel.closeFuture().addListener(future -> {
                if (future.isSuccess()) {
                    log.info("Client close");
                }
            });

            eventExecutors.shutdownGracefully();
            try {
                quicStreamChannel.closeFuture().sync();
                quicChannel.closeFuture().sync();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


}
