package org.alps.core;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.frame.*;
import org.alps.core.proto.AlpsProtocol;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AlpsEnhancedSession implements AlpsSession {

    private final AlpsSession session;
    private final FrameCoders frameCoders;
    private final AlpsDataCoderFactory dataCoderFactory;
    private final FrameListeners frameListeners;

    final AlpsConfig config;
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public AlpsEnhancedSession(AlpsSession session, FrameCoders frameCoders, AlpsDataCoderFactory dataCoderFactory,
                               FrameListeners frameListeners, AlpsConfig config) {
        this.session = session;
        this.frameCoders = frameCoders;
        this.dataCoderFactory = dataCoderFactory;
        this.frameListeners = frameListeners;
        this.config = config;
    }


    public <T> T attr(String key) {
        return session.attr(key);
    }


    @Override
    public AlpsEnhancedSession attr(String key, Object value) {
        session.attr(key, value);
        return this;
    }

    /**
     * 获取下个请求Id
     *
     * @return
     */
    public int nextId() {
        var ret = idGenerator.getAndIncrement();
        var now = idGenerator.get();
        if (now > Integer.MAX_VALUE - 4) {
            idGenerator.compareAndSet(now, 1);
        }
        return ret;
    }

    @Override
    public short module() {
        return session.module();
    }

    @Override
    public InetAddress selfAddress() {
        return session.selfAddress();
    }

    @Override
    public InetAddress targetAddress() {
        return session.targetAddress();
    }

    @Override
    public void send(AlpsPacket protocol) {
        session.send(protocol);
    }

    @Override
    public void send(AlpsProtocol.AlpsPacket protocol) {
        session.send(protocol);
    }

    void receive(Frame frame) {
        this.frameListeners.receiveFrame(this, frame);
    }

    public void receive(AlpsPacket protocol) throws Exception {
        var frame = frameCoders.decode(protocol);
        receive(frame);
    }

    public static BroadcastCommand broadcast(List<AlpsEnhancedSession> sessions, int command) {
        if (sessions == null || sessions.isEmpty()) {
            throw new IllegalArgumentException("sessions为空");
        }
        return new BroadcastCommand(sessions, command);
    }

    public ForgetCommand forget(int command) {
        return new ForgetCommand(this, command);
    }

    public RequestCommand request(int command) {
        return new RequestCommand(this, command);
    }

    public IdleCommand idle() {
        return new IdleCommand(this);
    }

    public ErrorCommand error() {
        return new ErrorCommand(this);
    }

    public ResponseCommand response() {
        return new ResponseCommand(this);
    }

    @Override
    public void close() {
        session.close();
    }

    public static class BaseCommand {
        final AlpsEnhancedSession session;


        final AlpsConfig config;

        AlpsMetadataBuilder metadataBuilder;
        AlpsDataBuilder dataBuilder;


        public BaseCommand(AlpsEnhancedSession session) {
            this.session = session;
            this.config = session.config;

            var code = config.getDataConfig().getCoder().getCode();
            AlpsConfig.ModuleConfig moduleConfig;
            if (session.module() == AlpsPacket.ZERO_MODULE) {
                moduleConfig = AlpsConfig.ModuleConfig.ZERO;
            } else {
                moduleConfig = config.getModules().stream()
                        .filter(e -> e.getModule() == session.module())
                        .findFirst().orElseThrow();
            }

            this.metadataBuilder = new AlpsMetadataBuilder().isZip(config.getMetaDataConfig().isEnabledZip())
                    .verifyToken(moduleConfig.getVerifyToken())
                    .version(moduleConfig.getVersion())
                    .containerCoder(code)
                    .coder(session.dataCoderFactory.getCoder(code));


            var code2 = config.getDataConfig().getCoder().getCode();
            this.dataBuilder = new AlpsDataBuilder()
                    .isZip(config.getDataConfig().isEnabledZip())
                    .dataCoder(code2)
                    .coder(session.dataCoderFactory.getCoder(code2));
        }

        public BaseCommand data(Object... data) {
            this.dataBuilder.addData(data);
            return this;
        }

        public BaseCommand metadata(String key, Object value) {
            metadataBuilder.addMetadata(key, value);
            return this;
        }
    }

    public static class BaseCommand2 extends BaseCommand {

        final int command;

        public BaseCommand2(AlpsEnhancedSession session, int command) {
            super(session);
            this.command = command;
        }

    }

    public static class ForgetCommand extends BaseCommand2 {

        public ForgetCommand(AlpsEnhancedSession session, int command) {
            super(session, command);
        }

        @Override
        public ForgetCommand data(Object... data) {
            super.data(data);
            return this;
        }

        @Override
        public ForgetCommand metadata(String key, Object value) {
            super.metadata(key, value);
            return this;
        }

        public CompletableFuture<Void> send() {
            return CompletableFuture.runAsync(() -> {
                var id = session.nextId();
                var frameBytes = ForgetFrame.toBytes(command, id);
                var metadata = metadataBuilder
                        .frameType(FrameCoders.DefaultFrame.FORGET.frameType)
                        .frame(frameBytes)
                        .build();
                var data = dataBuilder.build();
                var protocol = session.frameCoders.encode(session.module(), new ForgetFrame(command, id, metadata, data));
                session.send(protocol);
            }).whenComplete((ret, error) -> {
                if (error != null) {
                    log.error("Error sending", error);
                }
            });
        }
    }

    public static class BroadcastCommand extends ForgetCommand {

        final Collection<AlpsEnhancedSession> sessions;

        public BroadcastCommand(List<AlpsEnhancedSession> sessions, int command) {
            super(sessions.get(0), command);
            this.sessions = sessions;
        }

        @Override
        public ForgetCommand data(Object... data) {
            super.data(data);
            return this;
        }

        @Override
        public ForgetCommand metadata(String key, Object value) {
            super.metadata(key, value);
            return this;
        }

        public CompletableFuture<Void> send() {
            return CompletableFuture.runAsync(() -> {
                //TODO id? 因为fnf id是没有效果
                var id = session.nextId();
                var frameBytes = ForgetFrame.toBytes(command, id);
                var metadata = metadataBuilder
                        .frameType(FrameCoders.DefaultFrame.FORGET.frameType)
                        .frame(frameBytes)
                        .build();
                var data = dataBuilder.build();
                var protocol = session.frameCoders.encode(session.module(), new ForgetFrame(command, id, metadata, data));
                AlpsUtils.broadcast(sessions, protocol);
            }).whenComplete((ret, error) -> {
                if (error != null) {
                    log.error("Error sending", error);
                }
            });
        }
    }

    public static class RequestCommand extends BaseCommand2 {


        public RequestCommand(AlpsEnhancedSession session, int command) {
            super(session, command);
        }

        @Override
        public RequestCommand data(Object... data) {
            super.data(data);
            return this;
        }

        @Override
        public RequestCommand metadata(String key, Object value) {
            super.metadata(key, value);
            return this;
        }

        public CompletableFuture<Response> send() {
            AtomicReference<FrameListener> listener = new AtomicReference<>(null);
            return CompletableFuture.supplyAsync(() -> {
                        var id = session.nextId();
                        var frameBytes = RequestFrame.toBytes(command, id);
                        var metadata = metadataBuilder
                                .frameType(FrameCoders.DefaultFrame.REQUEST.frameType)
                                .frame(frameBytes)
                                .build();
                        var data = dataBuilder.build();
                        var requestFrame = new RequestFrame(command, id, metadata, data);
                        var protocol = session.frameCoders.encode(session.module(), requestFrame);
                        var responseResult = new ResponseResult(session, requestFrame);
                        listener.set((session, frame) -> responseResult.receive(((ResponseFrame) frame)));
                        session.frameListeners.addFrameListener(ResponseFrame.class,
                                listener.get(), responseResult::isResult
                        );
                        session.send(protocol);
                        return responseResult;
                    })
                    .orTimeout(5, TimeUnit.SECONDS)
                    .thenComposeAsync(ResponseResult::result)
                    .thenApplyAsync(Response::new)
                    .whenComplete((res, err) -> {
                        if (listener.get() != null) {
                            session.frameListeners.removeFrameListener(ResponseFrame.class, listener.get());
                        }
                    }).whenComplete((ret, error) -> {
                        if (error != null) {
                            log.error("Error sending", error);
                        }
                    });
        }

        <T> T getResult(ResponseFrame frame, Class<T> responseClass) {
            var data = frame.data();
            if (data.dataArray().length == 0) {
                return null;
            }
            return data.dataArray()[0].object(responseClass);
        }
    }

    public static class Response {
        private final ResponseFrame frame;

        public Response(ResponseFrame frame) {
            this.frame = frame;
        }

        public AlpsMetadata metadata() {
            return frame.metadata();
        }

        public AlpsData data() {
            return frame.data();
        }

        public <T> Optional<T> data(Class<T> clazz) {
            return data(0, clazz);
        }

        public <T> Optional<T> data(int index, Class<T> clazz) {
            if (index < 0 || index >= frame.data().dataArray().length) {
                return Optional.empty();
            }
            return Optional.ofNullable(frame.data().dataArray()[index].object(clazz));
        }

        @SuppressWarnings("unchecked")
        public <T> Optional<T> metadata(String key, Class<T> clazz) {
            return frame.metadata().getValue(key, clazz).map(e -> (T) e);
        }
    }

    public static class IdleCommand extends BaseCommand {
        public IdleCommand(AlpsEnhancedSession session) {
            super(session);
        }

        public CompletableFuture<Void> send() {
            return CompletableFuture.runAsync(() -> {
                var id = session.nextId();
                var frameBytes = IdleFrame.toBytes(id);
                var metadata = metadataBuilder
                        .frameType(FrameCoders.DefaultFrame.IDLE.frameType)
                        .frame(frameBytes)
                        .build();
                var protocol = session.frameCoders.encode(session.module(), new IdleFrame(id, metadata));
                session.send(protocol);
            }).whenComplete((ret, error) -> {
                if (error != null) {
                    log.error("Error sending", error);
                }
            });
        }
    }

    public static class ErrorCommand extends BaseCommand {

        private short code;

        public ErrorCommand(AlpsEnhancedSession session) {
            super(session);
        }

        @Override
        public ErrorCommand data(Object... data) {
            return this;
        }

        @Override
        public ErrorCommand metadata(String key, Object value) {
            return (ErrorCommand) super.metadata(key, value);
        }

        public ErrorCommand code(short code) {
            this.code = code;
            return this;
        }

        public CompletableFuture<Void> send() {
            return CompletableFuture.runAsync(() -> {
                var id = session.nextId();
                var frameBytes = ErrorFrame.toBytes(id, code);
                var metadata = metadataBuilder
                        .frameType(FrameCoders.DefaultFrame.ERROR.frameType)
                        .frame(frameBytes)
                        .build();
                var data = dataBuilder.build();
                var protocol = session.frameCoders.encode(AlpsPacket.ZERO_MODULE, new ErrorFrame(id, code, metadata, data));
                session.send(protocol);
            }).whenComplete((ret, error) -> {
                if (error != null) {
                    log.error("Error sending", error);
                }
            });
        }
    }

    public static class ResponseCommand extends BaseCommand {

        private int reqId;

        public ResponseCommand(AlpsEnhancedSession session) {
            super(session);
        }

        @Override
        public ResponseCommand data(Object... data) {
            super.data(data);
            return this;
        }

        @Override
        public ResponseCommand metadata(String key, Object value) {
            super.metadata(key, value);
            return this;
        }

        public ResponseCommand reqId(int reqId) {
            this.reqId = reqId;
            return this;
        }

        public CompletableFuture<Void> send() {
            return CompletableFuture.runAsync(() -> {
                var id = session.nextId();
                var frameBytes = ResponseFrame.toBytes(id, reqId);
                var metadata = metadataBuilder
                        .frameType(FrameCoders.DefaultFrame.RESPONSE.frameType)
                        .frame(frameBytes)
                        .build();
                var data = dataBuilder.build();
                var protocol = session.frameCoders.encode(session.module(), new ResponseFrame(id, reqId, metadata, data));
                session.send(protocol);
            }).whenComplete((ret, error) -> {
                if (error != null) {
                    log.error("Error sending", error);
                }
            });
        }
    }

    static class ResponseResult {

        final AlpsSession session;
        final RequestFrame requestFrame;
        final CompletableFuture<ResponseFrame> result;

        public ResponseResult(AlpsSession session, RequestFrame requestFrame) {
            this.session = session;
            this.requestFrame = requestFrame;
            result = new CompletableFuture<>();
        }

        public CompletableFuture<ResponseFrame> result() {
            return result;
        }

        void receive(ResponseFrame frame) {
            result.complete(frame);
        }

        boolean isResult(AlpsSession session, Frame frame) {
            if (frame instanceof ResponseFrame responseFrame) {
                return session.equals(this.session) && responseFrame.reqId() == requestFrame.id();
            }
            return false;
        }
    }
}
