package org.alps.core;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.common.AlpsException;
import org.alps.core.frame.*;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.support.AlpsDataBuilder;
import org.alps.core.support.AlpsMetadataBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AlpsEnhancedSession implements AlpsSession {

    private final AlpsSession session;
    private final FrameCoders frameCoders;
    private final AlpsDataCoderFactory dataCoderFactory;
    private final FrameListeners frameListeners;

    private final SessionListeners sessionListeners;

    final AlpsConfig config;
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private static final Scheduler ioScheduler = Schedulers.newParallel(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactory() {

        private final AtomicInteger id = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            var thread = new Thread(r);
            thread.setName("alps-thread-" + id.getAndIncrement());
            return thread;
        }
    });

    public AlpsEnhancedSession(AlpsSession session, FrameCoders frameCoders, AlpsDataCoderFactory dataCoderFactory,
                               FrameListeners frameListeners, SessionListeners sessionListeners, AlpsConfig config) {
        this.session = session;
        this.frameCoders = frameCoders;
        this.dataCoderFactory = dataCoderFactory;
        this.frameListeners = frameListeners;
        this.sessionListeners = sessionListeners;
        this.config = config;
        sessionListeners.connect(this);
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
    public String module() {
        return session.module();
    }

    @Override
    public Optional<String> selfAddress() {
        return session.selfAddress();
    }

    @Override
    public Optional<String> targetAddress() {
        return session.targetAddress();
    }

    @Override
    public void send(AlpsPacket protocol) {
        Thread.startVirtualThread(() -> session.send(protocol));
    }

    @Override
    public void send(AlpsProtocol.AlpsPacket protocol) {
        Thread.startVirtualThread(() -> session.send(protocol));
    }

    @Override
    public void send(byte[] data) {
        session.send(data);
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
        return new BroadcastCommand(sessions, command, ioScheduler);
    }

    public ForgetCommand forget(int command) {
        return new ForgetCommand(this, command, ioScheduler);
    }

    public RequestCommand request(int command) {
        return new RequestCommand(this, command, ioScheduler);
    }

    public IdleCommand idle() {
        return new IdleCommand(this, ioScheduler);
    }

    public ErrorCommand error() {
        return new ErrorCommand(this, ioScheduler);
    }

    public ResponseCommand response() {
        return new ResponseCommand(this, ioScheduler);
    }

    public StreamRequestCommand streamRequest(int command) {
        return new StreamRequestCommand(this, command, ioScheduler);
    }

    public StreamResponseCommand streamResponse() {
        return new StreamResponseCommand(this, ioScheduler);
    }

    @Override
    public void close() {
        if (!session.isClose()) {
            session.close();
            sessionListeners.disconnect(this);
        }
    }

    @Override
    public boolean isClose() {
        return session.isClose();
    }

    public static class BaseCommand {
        final AlpsEnhancedSession session;


        final AlpsConfig config;

        final Scheduler ioScheduler;

        AlpsMetadataBuilder metadataBuilder;
        AlpsDataBuilder dataBuilder;


        public BaseCommand(AlpsEnhancedSession session, Scheduler ioScheduler) {
            this.ioScheduler = ioScheduler;
            this.session = session;
            this.config = session.config;

            var code = config.getDataConfig().getCoder().getCode();

            this.metadataBuilder = new AlpsMetadataBuilder().isZip(config.getMetaDataConfig().isEnabledZip())
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

        public BaseCommand2(AlpsEnhancedSession session, int command, Scheduler ioScheduler) {
            super(session, ioScheduler);
            this.command = command;
        }

    }

    public static class ForgetCommand extends BaseCommand2 {

        public ForgetCommand(AlpsEnhancedSession session, int command, Scheduler ioScheduler) {
            super(session, command, ioScheduler);
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

        public void send() {
            Thread.startVirtualThread(() -> {
                try {
                    var frameBytes = ForgetFrame.toBytes(command);
                    var metadata = metadataBuilder
                            .frameType(FrameCoders.DefaultFrame.FORGET.frameType)
                            .frame(frameBytes)
                            .build();
                    var data = dataBuilder.build();
                    var protocol = session.frameCoders.encode(config.getSocketType(), session.module(),
                            new ForgetFrame(command, metadata, data, null));
                    session.send(protocol);
                } catch (Exception ex) {
                    log.error("Error sending", ex);
                    throw new AlpsException(ex);
                }
            });
        }
    }

    public static class BroadcastCommand extends ForgetCommand {

        final Collection<AlpsEnhancedSession> sessions;

        public BroadcastCommand(List<AlpsEnhancedSession> sessions, int command, Scheduler ioScheduler) {
            super(sessions.get(0), command, ioScheduler);
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

        public void send() {
            Thread.startVirtualThread(() -> {
                try {
                    var frameBytes = ForgetFrame.toBytes(command);
                    var metadata = metadataBuilder
                            .frameType(FrameCoders.DefaultFrame.FORGET.frameType)
                            .frame(frameBytes)
                            .build();
                    var data = dataBuilder.build();
                    var protocol = session.frameCoders.encode(config.getSocketType(), session.module(),
                            new ForgetFrame(command, metadata, data, null));
                    AlpsUtils.broadcast(sessions, protocol);
                } catch (Exception ex) {
                    log.error("Error sending", ex);
                    throw new AlpsException(ex);
                }
            });
        }
    }

    public static class RequestCommand extends BaseCommand2 {


        public RequestCommand(AlpsEnhancedSession session, int command, Scheduler ioScheduler) {
            super(session, command, ioScheduler);
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

        public <T> T send(Class<T> clazz) {
            AtomicReference<T> result = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            Thread.startVirtualThread(() -> {
                FrameListener listener = null;
                try {
                    var id = session.nextId();
                    var frameBytes = RequestFrame.toBytes(command, id);
                    var metadata = metadataBuilder
                            .frameType(FrameCoders.DefaultFrame.REQUEST.frameType)
                            .frame(frameBytes)
                            .build();
                    var data = dataBuilder.build();
                    var requestFrame = new RequestFrame(command, id, metadata, data, null);
                    var protocol = session.frameCoders.encode(config.getSocketType(), session.module(), requestFrame);
                    var responseResult = new ResponseResult(session, requestFrame);
                    listener = (session, frame) -> responseResult.receive(((ResponseFrame) frame));
                    session.frameListeners.addFrameListener(ResponseFrame.class,
                            listener, responseResult::isResult
                    );
                    session.send(protocol);

                    responseResult.countDownLatch.await(5L, TimeUnit.SECONDS);
                    var response = new Response(responseResult.result().orElseThrow());
                    result.set(response.data(clazz).orElse(null));
                } catch (InterruptedException e) {
                    log.error("Error sending", e);
                    throw new AlpsException(e);
                } finally {
                    if (listener != null) {
                        session.frameListeners.removeFrameListener(ResponseFrame.class, listener);
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("Error sending", e);
                throw new AlpsException(e);
            }
            return result.get();
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
        public IdleCommand(AlpsEnhancedSession session, Scheduler ioScheduler) {
            super(session, ioScheduler);
        }

        public void send() {
            Thread.startVirtualThread(() -> {
                try {
                    var frameBytes = IdleFrame.toIdleBytes();
                    var metadata = metadataBuilder
                            .frameType(FrameCoders.DefaultFrame.IDLE.frameType)
                            .frame(frameBytes)
                            .build();
                    var protocol = session.frameCoders.encode(config.getSocketType(), session.module(),
                            new IdleFrame(metadata, null));
                    session.send(protocol);
                } catch (Exception ex) {
                    log.error("Error sending", ex);
                    throw new AlpsException(ex);
                }
            });
        }
    }

    public static class ErrorCommand extends BaseCommand {

        private int code;

        public ErrorCommand(AlpsEnhancedSession session, Scheduler ioScheduler) {
            super(session, ioScheduler);
        }

        @Override
        public ErrorCommand data(Object... data) {
            return this;
        }

        @Override
        public ErrorCommand metadata(String key, Object value) {
            return (ErrorCommand) super.metadata(key, value);
        }

        public ErrorCommand code(int code) {
            this.code = code;
            return this;
        }

        public void send() {
            Thread.startVirtualThread(() -> {
                try {
                    var frameBytes = ErrorFrame.toBytes(code);
                    var metadata = metadataBuilder
                            .frameType(FrameCoders.DefaultFrame.ERROR.frameType)
                            .frame(frameBytes)
                            .build();
                    var data = dataBuilder.build();
                    var protocol = session.frameCoders.encode(config.getSocketType(), AlpsPacket.ZERO_MODULE,
                            new ErrorFrame(code, metadata, data, null));
                    session.send(protocol);
                } catch (Exception ex) {
                    log.error("Error sending", ex);
                    throw new AlpsException(ex);
                }
            });
        }
    }

    public static class ResponseCommand extends BaseCommand {

        private int reqId;

        public ResponseCommand(AlpsEnhancedSession session, Scheduler ioScheduler) {
            super(session, ioScheduler);
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

        public void send() {
            Thread.startVirtualThread(() -> {
                try {
                    var frameBytes = ResponseFrame.toBytes(reqId);
                    var metadata = metadataBuilder
                            .frameType(FrameCoders.DefaultFrame.RESPONSE.frameType)
                            .frame(frameBytes)
                            .build();
                    var data = dataBuilder.build();
                    var protocol = session.frameCoders.encode(config.getSocketType(), session.module(),
                            new ResponseFrame(reqId, metadata, data, null));
                } catch (Exception ex) {
                    log.error("Error sending", ex);
                    throw new AlpsException(ex);
                }
            });
        }
    }

    static class ResponseResult {

        final AlpsSession session;
        final RequestFrame requestFrame;
        final CountDownLatch countDownLatch;
        final AtomicReference<ResponseFrame> result;

        public ResponseResult(AlpsSession session, RequestFrame requestFrame) {
            this.session = session;
            this.requestFrame = requestFrame;
            this.countDownLatch = new CountDownLatch(1);
            result = new AtomicReference<>(null);
        }

        public Optional<ResponseFrame> result() {
            return Optional.ofNullable(result.get());
        }

        void receive(ResponseFrame frame) {
            this.result.set(frame);
            this.countDownLatch.countDown();
        }

        boolean isResult(AlpsSession session, Frame frame) {
            if (frame instanceof ResponseFrame responseFrame) {
                return session.equals(this.session) && responseFrame.reqId() == requestFrame.id();
            }
            return false;
        }
    }

    public static class StreamRequestCommand extends BaseCommand2 {


        public StreamRequestCommand(AlpsEnhancedSession session, int command, Scheduler ioScheduler) {
            super(session, command, ioScheduler);
        }

        @Override
        public StreamRequestCommand data(Object... data) {
            super.data(data);
            return this;
        }

        @Override
        public StreamRequestCommand metadata(String key, Object value) {
            super.metadata(key, value);
            return this;
        }

        public <T> Flux<T> send(Class<T> clazz) {
            AtomicReference<FrameListener> listener = new AtomicReference<>(null);
            var many = Sinks.many().unicast().<T>onBackpressureBuffer();
            Mono.fromRunnable(() -> {
                        var id = session.nextId();
                        var frameBytes = StreamRequestFrame.toBytes(command, id);
                        var metadata = metadataBuilder
                                .frameType(FrameCoders.DefaultFrame.STREAM_REQUEST.frameType)
                                .frame(frameBytes)
                                .build();
                        var data = dataBuilder.build();
                        var requestFrame = new StreamRequestFrame(id, command, metadata, data, null);
                        var protocol = session.frameCoders.encode(config.getSocketType(), session.module(), requestFrame);
                        // 注册监听
                        listener.set((session, frame) -> {
                            var streamResponseFrame = (StreamResponseFrame) frame;
                            if (streamResponseFrame.finish()) {
                                many.tryEmitComplete();
                                ((AlpsEnhancedSession) session).frameListeners
                                        .removeFrameListener(StreamResponseFrame.class, listener.get());
                            } else {
                                var t = new ResponseStream(streamResponseFrame).data(clazz);
                                t.ifPresent(many::tryEmitNext);
                            }
                        });
                        session.frameListeners.addFrameListener(StreamResponseFrame.class,
                                listener.get(), (AlpsSession session, Frame frame) -> {
                                    if (frame instanceof StreamResponseFrame responseFrame) {
                                        return session.equals(this.session) && responseFrame.reqId() == requestFrame.id();
                                    }
                                    return false;
                                }
                        );
                        session.send(protocol);
                    }).publishOn(ioScheduler)
                    .doOnError(error -> log.error("Error sending", error))
                    .subscribe();
            return many.asFlux();
        }

    }

    public static class StreamResponseCommand extends BaseCommand {

        private int reqId;
        private boolean finish;

        public StreamResponseCommand(AlpsEnhancedSession session, Scheduler ioScheduler) {
            super(session, ioScheduler);
        }

        @Override
        public StreamResponseCommand data(Object... data) {
            super.data(data);
            return this;
        }

        @Override
        public StreamResponseCommand metadata(String key, Object value) {
            super.metadata(key, value);
            return this;
        }

        public StreamResponseCommand reqId(int reqId) {
            this.reqId = reqId;
            return this;
        }

        public StreamResponseCommand finish(boolean finish) {
            this.finish = finish;
            return this;
        }

        public Mono<Void> send() {
            return Mono.fromRunnable(() -> {
                        var frameBytes = StreamResponseFrame.toBytes(reqId, finish);
                        var metadata = metadataBuilder
                                .frameType(FrameCoders.DefaultFrame.STREAM_RESPONSE.frameType)
                                .frame(frameBytes)
                                .build();
                        var data = dataBuilder.build();
                        var protocol = session.frameCoders.encode(config.getSocketType(), session.module(),
                                new StreamResponseFrame(reqId, finish, metadata, data, null));
                        session.send(protocol);
                    }).publishOn(ioScheduler).doOnError(error -> log.error("Error sending", error))
                    .then();
        }
    }

    public static class ResponseStream {
        private final StreamResponseFrame frame;

        public ResponseStream(StreamResponseFrame frame) {
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
}
