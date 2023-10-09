package org.alps.starter;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsEnhancedSession;
import org.alps.core.CommandFrame;
import org.alps.core.Router;
import org.alps.core.common.AlpsException;
import org.alps.core.frame.RequestFrame;
import org.alps.core.frame.StreamRequestFrame;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.alps.starter.anno.Metadata;
import org.alps.starter.anno.RawPacket;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Function;

@Slf4j
record StreamRouter(String module, int command, Object target, Method method) implements Router {

    public static StreamRouter create(Object target, Method method, List<String> properties) {
        return Utils.create(target, method, properties, StreamRouter::new);
    }


    @Override
    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Throwable {
        var alpsExchange = new AlpsExchange(session, frame.metadata(), frame.data());
        var descriptor = MethodDescriptor.create(alpsExchange, target, method);
        var ret = descriptor.invoke(frame);
        if (ret == null) {
            session.streamResponse()
                    .reqId(((StreamRequestFrame) frame).id())
                    .finish(true)
                    .send();
            return;
        }
        if (ret instanceof Flow.Publisher<?> flux) {
            flux.subscribe(new Flow.Subscriber<Object>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {

                }

                @Override
                public void onNext(Object item) {
                    var responseCommand = session.streamResponse().reqId(((StreamRequestFrame) frame).id());
                    if (!alpsExchange.getMetadata().isEmpty()) {
                        alpsExchange.getMetadata().forEach(responseCommand::metadata);
                    }
                    responseCommand.data(item);
                    responseCommand.send();
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("stream error", throwable);
                }

                @Override
                public void onComplete() {
                    session.streamResponse()
                            .reqId(((StreamRequestFrame) frame).id())
                            .finish(true)
                            .send();
                }
            });
        }
    }
}

record RequestRouter(String module, int command, Object target, Method method) implements Router {

    public static RequestRouter create(Object target, Method method, List<String> properties) {
        return Utils.create(target, method, properties, RequestRouter::new);
    }


    @Override
    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Throwable {
        var alpsExchange = new AlpsExchange(session, frame.metadata(), frame.data());
        var descriptor = MethodDescriptor.create(alpsExchange, target, method);
        var ret = descriptor.invoke(frame);
        var responseCommand = session.response().reqId(((RequestFrame) frame).id());
        if (!alpsExchange.getMetadata().isEmpty()) {
            alpsExchange.getMetadata().forEach(responseCommand::metadata);
        }
        if (ret != null) {
            responseCommand.data(ret);
        }
        responseCommand.send();
    }
}

record ForgetRouter(String module, int command, Object target, Method method) implements Router {

    public static ForgetRouter create(Object target, Method method, List<String> properties) {
        return Utils.create(target, method, properties, ForgetRouter::new);
    }

    @Override
    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Throwable {
        var alpsExchange = new AlpsExchange(session, frame.metadata(), frame.data());
        var descriptor = MethodDescriptor.create(alpsExchange, target, method);
        descriptor.invoke(frame);
    }
}

interface NewRouter<T> {
    T create(String module, int command, Object target, Method method);
}

class Utils {
    static <T> T create(Object target, Method method, List<String> properties, NewRouter<T> router) {
        var annotation = Objects.requireNonNull(method.getAnnotation(Command.class), "@Command为空");
        var command = annotation.command();
        var module = Optional.of(Objects.requireNonNull(
                        method.getDeclaringClass().getAnnotation(AlpsModule.class), "@AlpsModule为空").module())
                .flatMap(e -> properties.stream().filter(ele -> ele.equals(e)).findFirst())
                .orElseThrow(() -> new IllegalArgumentException("模块没有配置" + method.getName()));
        method.setAccessible(true);
        return router.create(module, command, target, method);
    }
}

record MethodDescriptor(AlpsExchange exchange, Object target, MethodHandle method,
                        List<Function<CommandFrame, Object>> suppliers) {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static MethodDescriptor create(AlpsExchange exchange, Object target, Method method) {
        var parameters = method.getParameters();
        List<Function<CommandFrame, Object>> suppliers = new ArrayList<>(parameters.length);
        int index = 0;
        for (Parameter parameter : parameters) {
            if (parameter.getType().equals(AlpsExchange.class)) {
                suppliers.add(frame -> exchange);
                continue;
            }
            var metadataAnnotation = parameter.getAnnotation(Metadata.class);
            var rawPacketAnnotation = parameter.getAnnotation(RawPacket.class);
            if (metadataAnnotation != null) {
                var key = metadataAnnotation.value();
                suppliers.add(frame -> frame.metadata().getValue(key, parameter.getType()));
            } else if (rawPacketAnnotation != null) {
                // 如果是获取，保证一定有
                suppliers.add(frame -> frame.rawPacket().orElseThrow());
            } else {
                var i = index++;
                suppliers.add(frame -> frame.data().dataArray()[i].object(parameter.getType()));
            }
        }
        try {
            var methodHandle = LOOKUP.findVirtual(target.getClass(), method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
            return new MethodDescriptor(exchange, target, methodHandle, suppliers);
        } catch (Throwable ex) {
            throw new AlpsException(ex);
        }
    }

    Object invoke(CommandFrame frame) throws Throwable {
        Object[] args = new Object[suppliers.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = suppliers.get(i).apply(frame);
        }
        return method.invoke(target, args);
    }
}
