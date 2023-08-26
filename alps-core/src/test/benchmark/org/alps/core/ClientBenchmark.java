package org.alps.core;

import com.google.protobuf.StringValue;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.frame.ForgetFrame;
import org.alps.core.frame.RequestFrame;
import org.alps.core.frame.ResponseFrame;
import org.alps.core.socket.netty.client.AbstractAlpsClient;
import org.alps.core.socket.netty.client.AlpsQuicClient;
import org.alps.core.socket.netty.client.NettyClientConfig;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@Fork(1)
@Slf4j
public class ClientBenchmark {

    @Benchmark
    public void forget(SessionState state) throws ExecutionException, InterruptedException {
        state.session.forget(1)
                .data(StringValue.of("1234".repeat(1024)))
                .send().get();
    }

    @Benchmark
//    @Threads(32)
    public void request(SessionState state) throws ExecutionException, InterruptedException {
        var ret = state.session.request(1)
                .data(StringValue.of("1234".repeat(1024)))
                .send()
                .thenApply(response -> response.data(0, StringValue.class).orElse(null))
                .get();
//        log.info("{}", ret.getValue());
    }


    @State(Scope.Benchmark)
    public static class SessionState {
        AlpsEnhancedSession session;

        AbstractAlpsClient client;

        @Setup
        public void setup() {
            var dataCoderFactory = new AlpsDataCoderFactory();
            var frameFactory = new FrameCoders(dataCoderFactory);
            var routerDispatcher = new RouterDispatcher();
            var listenerHandler = new FrameListeners(routerDispatcher);
            var config = new AlpsConfig();
            config.getDataConfig().setEnabledZip(true);
            for (int i = 0; i < 10; i++) {
                short module = (short) (i + 1);
                config.getModules().add(new AlpsConfig.ModuleConfig((short) i, (short) 1, 1L));
                for (int j = 0; j < 20; j++) {
                    short command = (short) (j + 1);
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
                            if (frame instanceof ForgetFrame forgetFrame) {

                            } else if (frame instanceof RequestFrame requestFrame) {
                                var metadata = requestFrame.metadata();
                                int id = session.nextId();
                                session.receive(new ResponseFrame(id, requestFrame.id(),
                                        new AlpsMetadataBuilder().isZip(metadata.isZip())
                                                .verifyToken(metadata.verifyToken())
                                                .version(metadata.version())
                                                .containerCoder(metadata.containerCoder())
                                                .coder(metadata.coder())
                                                .frameType(FrameCoders.DefaultFrame.RESPONSE.frameType)
                                                .frame(ResponseFrame.toBytes(id, requestFrame.id()))
                                                .build()
                                        , requestFrame.data()));
                            }
                        }
                    });
                }
            }

            var nettyServerConfig = new NettyClientConfig();
            nettyServerConfig.setPort(6195);
            nettyServerConfig.setHost("127.0.0.1");

            var enhancedSessionFactory = new DefaultEnhancedSessionFactory(frameFactory, dataCoderFactory, listenerHandler, config);
//            this.client = new AlpsTcpClient(new NioEventLoopGroup(), nettyServerConfig, enhancedSessionFactory,
//                    enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList(), dataCoderFactory);

            this.client = new AlpsQuicClient(new NioEventLoopGroup(2), nettyServerConfig, enhancedSessionFactory,
                    enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList(), dataCoderFactory);


            client.start();
            while (client.isNotReady()) {
            }
            this.session = client.session((short) 1).map(e -> ((AlpsEnhancedSession) e)).get();
        }

        @TearDown
        public void tearDown() {
            client.close();
        }

    }
}
