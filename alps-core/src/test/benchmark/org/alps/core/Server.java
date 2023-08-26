package org.alps.core;

import com.google.protobuf.StringValue;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.frame.ForgetFrame;
import org.alps.core.frame.RequestFrame;
import org.alps.core.socket.netty.server.AlpsQuicServer;
import org.alps.core.socket.netty.server.NettyServerConfig;
import org.alps.core.socket.netty.server.QuicServerConfig;

import java.util.Collections;
import java.util.Map;

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
        var routerDispatcher = new RouterDispatcher();
        var config = new AlpsConfig();
        config.getDataConfig().setEnabledZip(true);
        for (int i = 0; i < 10; i++) {
            short module = (short) i;
            config.getModules().add(new AlpsConfig.ModuleConfig((short) i, (short) 1, 1L));
            for (int j = 0; j < 20; j++) {
                short command = (short) j;
                routerDispatcher.addRouter(new Router() {
                    @Override
                    public short module() {
                        return module;
                    }

                    @Override
                    public int command() {
                        return command;
                    }

                    @Override
                    public void handle(AlpsEnhancedSession session, CommandFrame frame) {
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
                                    .reqId(frame.id())
                                    .data(StringValue.of("Hello"))
                                    .send();
                        }
                    }
                });
            }
        }
        var enhancedSessionFactory = new DefaultEnhancedSessionFactory(routerDispatcher, config);
//        var server = new AlpsTcpServer(new NioEventLoopGroup(1),
//                new NioEventLoopGroup(32),
//                new NioEventLoopGroup(32),
//                nettyServerConfig, enhancedSessionFactory,
//                enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList(), enhancedSessionFactory.dataCoderFactory);

        var certificate = new SelfSignedCertificate();
        var quicServerConfig = new QuicServerConfig(certificate.key(), null, Collections.singletonList(certificate.cert()));
        var server = new AlpsQuicServer(new NioEventLoopGroup(16),
                new NioEventLoopGroup(32),
                nettyServerConfig, quicServerConfig, enhancedSessionFactory,
                enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList(),
                enhancedSessionFactory.dataCoderFactory);
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
            this.frameCoders = new FrameCoders(dataCoderFactory);
        }

        @Override
        public AlpsEnhancedSession create(AlpsSession session) {
            return new AlpsEnhancedSession(session, frameCoders, dataCoderFactory, frameListeners, config);
        }
    }
}
