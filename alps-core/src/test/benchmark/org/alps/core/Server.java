package org.alps.core;

import com.google.protobuf.StringValue;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.frame.ForgetFrame;
import org.alps.core.frame.RequestFrame;
import org.alps.core.frame.StreamRequestFrame;
import org.alps.core.socket.netty.server.AlpsTcpServer;
import org.alps.core.socket.netty.server.NettyServerConfig;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class Server {

    public static void main(String[] args) throws Exception {
        var nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setPort(6195);
        nettyServerConfig.setOptionSettings(Map.of(
                ChannelOption.SO_BACKLOG, 128
        ));
        nettyServerConfig.setChildOptionSettings(Map.of(
                ChannelOption.SO_KEEPALIVE, true
        ));
        var timeout = new NettyServerConfig.Timeout();
        timeout.setAllIdleTime(5000);
        timeout.setReaderIdleTime(5000);
        timeout.setWriterIdleTime(5000);
        nettyServerConfig.setTimeout(timeout);
        nettyServerConfig.setTimeout(new NettyServerConfig.Timeout(5000, 5000, 5000));
        var routerDispatcher = new RouterDispatcher();
        var config = new AlpsConfig();
        config.getDataConfig().setEnabledZip(true);
        for (int i = 0; i < 10; i++) {
            var module = "" + i;
            config.getModules().add(module);
            for (int j = 0; j < 20; j++) {
                short command = (short) j;
                routerDispatcher.addRouter(new Router() {
                    @Override
                    public String module() {
                        return module;
                    }

                    @Override
                    public int command() {
                        return command;
                    }

                    @Override
                    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Throwable {
                        log.info("1: {}", frame.getClass());
//                        log.info("Command Received: " + command);
//                        try {
//                            TimeUnit.MICROSECONDS.sleep(20L);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
                        if (frame instanceof ForgetFrame forgetFrame) {
//                            AlpsEnhancedSession.broadcast(Collections.singletonList(session), 1)
//                                    .data(StringValue.of("11111"))
//                                    .send();
//                            log.info("send");
                        } else if (frame instanceof RequestFrame requestFrame) {
                            session.response()
                                    .reqId(requestFrame.id())
                                    .data(StringValue.of("Hello"))
                                    .send();
                        } else if (frame instanceof StreamRequestFrame streamRequestFrame) {
                            AtomicReference<Disposable> disposable = new AtomicReference<>();
                            disposable.set(Flux.interval(Duration.ofSeconds(1L))
                                    .publishOn(Schedulers.boundedElastic())
                                    .doOnNext(n -> {
                                        if (n == 5) {
                                            session.streamResponse()
                                                    .reqId(streamRequestFrame.id())
                                                    .finish(true)
                                                    .send();
                                        }
                                        if (session.isClose() && disposable.get() != null) {
                                            disposable.get().dispose();
                                        }
                                        session.streamResponse()
                                                .reqId(streamRequestFrame.id())
                                                .data(StringValue.of("Hello" + n))
                                                .send();
                                        log.info("send: {} {}", n, session.isClose());
                                    }).subscribe());
                        }
                    }
                });
            }
        }
        var enhancedSessionFactory = new DefaultEnhancedSessionFactory(routerDispatcher, config);
        var server = new AlpsTcpServer(new NioEventLoopGroup(1),
                new NioEventLoopGroup(32),
                nettyServerConfig, enhancedSessionFactory,
                enhancedSessionFactory.config.getModules(), enhancedSessionFactory.dataCoderFactory);

//        var certificate = new SelfSignedCertificate();
//        var quicServerConfig = new QuicServerConfig(certificate.key(), null, Collections.singletonList(certificate.cert()));
//        var server = new AlpsQuicServer(new NioEventLoopGroup(16),
//                nettyServerConfig, quicServerConfig, enhancedSessionFactory,
//                enhancedSessionFactory.config.getModules(),
//                enhancedSessionFactory.dataCoderFactory);
        server.start();
    }

    static class DefaultEnhancedSessionFactory implements EnhancedSessionFactory {

        final FrameCoders frameCoders;
        final AlpsDataCoderFactory dataCoderFactory;
        final FrameListeners frameListeners;
        final AlpsConfig config;

        DefaultEnhancedSessionFactory(RouterDispatcher routerDispatcher, AlpsConfig config) {
            this.dataCoderFactory = new AlpsDataCoderFactory();
            this.frameListeners = new FrameListeners(routerDispatcher);
            this.config = config;
            this.frameCoders = new FrameCoders();
        }

        @Override
        public AlpsEnhancedSession create(AlpsSession session) {
            return new AlpsEnhancedSession(session, frameCoders, dataCoderFactory, frameListeners, new SessionListeners(Collections.emptyList()), config);
        }
    }
}
