package org.alps.starter;

import org.alps.core.AlpsEnhancedSession;
import org.alps.core.CommandFrame;
import org.alps.core.Router;
import org.alps.core.frame.RequestFrame;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.alps.starter.anno.Metadata;
import org.alps.starter.anno.RawPacket;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


record RequestRouter(short module, int command, Object target, Method method) implements Router {

    public static RequestRouter create(Object target, Method method, List<AlpsProperties.ModuleProperties> properties) {
        return Utils.create(target, method, properties, RequestRouter::new);
    }


    @Override
    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Exception {
        var alpsExchange = new AlpsExchange(session, frame.metadata(), frame.data());
        var descriptor = MethodDescriptor.create(alpsExchange, target, method);
        var ret = descriptor.invoke(frame);
        var responseCommand = session.response().reqId(((RequestFrame) frame).id());
        if (ret != null) {
            responseCommand.data(ret);
        }
        if (!alpsExchange.getMetadata().isEmpty()) {
            alpsExchange.getMetadata().forEach(responseCommand::metadata);
        }
        responseCommand.send();
    }
}

record ForgetRouter(short module, int command, Object target, Method method) implements Router {

    public static ForgetRouter create(Object target, Method method, List<AlpsProperties.ModuleProperties> properties) {
        return Utils.create(target, method, properties, ForgetRouter::new);
    }

    @Override
    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Exception {
        var alpsExchange = new AlpsExchange(session, frame.metadata(), frame.data());
        var descriptor = MethodDescriptor.create(alpsExchange, target, method);
        descriptor.invoke(frame);
    }
}

interface NewRouter<T> {
    T create(short module, int command, Object target, Method method);
}

class Utils {
    static <T> T create(Object target, Method method, List<AlpsProperties.ModuleProperties> properties, NewRouter<T> router) {
        var annotation = Objects.requireNonNull(method.getAnnotation(Command.class), "@Command为空");
        var command = annotation.command();
        var module = Optional.of(Objects.requireNonNull(
                        method.getDeclaringClass().getAnnotation(AlpsModule.class), "@AlpsModule为空").module())
                .flatMap(e -> properties.stream().filter(ele -> ele.getName().equals(e)).findFirst())
                .map(AlpsProperties.ModuleProperties::getCode)
                .orElseThrow(() -> new IllegalArgumentException("模块没有配置"));
        method.setAccessible(true);
        return router.create(module, command, target, method);
    }
}

record MethodDescriptor(AlpsExchange exchange, Object target, Method method,
                        List<Function<CommandFrame, Object>> suppliers) {

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
        return new MethodDescriptor(exchange, target, method, suppliers);
    }

    Object invoke(CommandFrame frame) throws Exception {
        Object[] args = new Object[suppliers.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = suppliers.get(i).apply(frame);
        }
        return method.invoke(target, args);
    }
}
