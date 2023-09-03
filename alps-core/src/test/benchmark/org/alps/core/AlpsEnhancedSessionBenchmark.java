package org.alps.core;

import org.alps.core.frame.ForgetFrame;
import org.alps.core.frame.RequestFrame;
import org.alps.core.frame.ResponseFrame;
import org.alps.core.proto.AlpsProtocol;
import org.openjdk.jmh.annotations.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@Fork(1)
public class AlpsEnhancedSessionBenchmark {

    @Benchmark
    public void forget(SessionState state) throws ExecutionException, InterruptedException {
        state.session.forget(1)
                .data("1234".repeat(1024))
                .send().block();
    }

    @Benchmark
    public void request(SessionState state) throws ExecutionException, InterruptedException {
        state.session.forget(1)
                .data("1234".repeat(1024))
                .send().block();
    }


    @State(Scope.Benchmark)
    public static class SessionState {
        AlpsEnhancedSession session;

        @Setup
        public void setup() {
            var session = new Session();
            var dataCoderFactory = new AlpsDataCoderFactory();
            var frameFactory = new FrameCoders(dataCoderFactory);
            var routerDispatcher = new RouterDispatcher();

            var config = new AlpsConfig();
            for (int i = 0; i < 10; i++) {
                var module = "" + i;
                config.getModules().add(new AlpsConfig.ModuleConfig(module, (short) 1, 1L));
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
                        public void handle(AlpsEnhancedSession session, CommandFrame frame) {
                            if (frame instanceof ForgetFrame forgetFrame) {

                            } else if (frame instanceof RequestFrame requestFrame) {
                                var metadata = requestFrame.metadata();
                                session.receive(new ResponseFrame(requestFrame.id(),
                                        new AlpsMetadataBuilder().isZip(metadata.isZip())
                                                .verifyToken(metadata.verifyToken())
                                                .version(metadata.version())
                                                .containerCoder(metadata.containerCoder())
                                                .coder(metadata.coder())
                                                .frameType(FrameCoders.DefaultFrame.RESPONSE.frameType)
                                                .frame(ResponseFrame.toBytes(requestFrame.id()))
                                                .build()
                                        , requestFrame.data(), null));
                            }
                        }
                    });
                }
            }
            var listenerHandler = new FrameListeners(routerDispatcher);
            this.session = new AlpsEnhancedSession(session, frameFactory, dataCoderFactory, listenerHandler, config);
        }

    }

    static final class Session implements AlpsSession {

        @Override
        public String module() {
            return "";
        }

        @Override
        public Optional<String> selfAddress() {
            return Optional.empty();
        }

        @Override
        public Optional<String> targetAddress() {
            return Optional.empty();
        }

        @Override
        public void send(AlpsPacket protocol) {

        }

        @Override
        public void send(AlpsProtocol.AlpsPacket protocol) {

        }

        @Override
        public void close() {

        }

        @Override
        public boolean isClose() {
            return false;
        }

        @Override
        public <T> T attr(String key) {
            return null;
        }

        @Override
        public AlpsSession attr(String key, Object value) {
            return this;
        }

        @Override
        public boolean isAuth() {
            return false;
        }

        @Override
        public void auth(int version, long verifyToken) {

        }
    }
}
