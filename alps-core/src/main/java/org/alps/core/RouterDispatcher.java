package org.alps.core;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.proto.Errors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 协议处理转发
 */
@Slf4j
public class RouterDispatcher {

    private final Map<String, Map<Integer, Router>> routes = new ConcurrentHashMap<>();
    private final List<RouterFilter> filters = new CopyOnWriteArrayList<>();

    private final List<UnknownRouter> unknownRouters = new CopyOnWriteArrayList<>();

    /**
     * 注册路由
     */
    public void addRouter(Router router) {
        routes.computeIfAbsent(router.module(), k -> new ConcurrentHashMap<>()).put(router.command(), router);
    }

    /**
     * 取消注册路由
     */
    public void removeRouter(Router router) {
        routes.computeIfAbsent(router.module(), k -> new ConcurrentHashMap<>()).remove(router.command());
    }

    public void addRouter(UnknownRouter router) {
        unknownRouters.add(router);
    }

    public void removeRouter(UnknownRouter router) {
        unknownRouters.remove(router);
    }

    public void addFilter(RouterFilter filter) {
        if (!filters.contains(filter)) {
            filters.add(filter);
        } else {
            throw new IllegalArgumentException("重复添加过滤器");
        }

    }

    public void dispatch(AlpsEnhancedSession session, CommandFrame frame) {
        for (RouterFilter filter : filters) {
            if (!filter.filter(session, frame)) {
                log.debug("Filter failed. {}", filter.getClass().getName());
                return;
            }
        }
        Thread.startVirtualThread(() -> {
            var module = session.module();
            int command = frame.command();
            if (routes.containsKey(module)) {
                var routerMap = routes.get(module);
                if (routerMap.containsKey(command)) {
                    var router = routerMap.get(command);
                    try {
                        router.handle(session, frame);
                        return;
                    } catch (Throwable e) {
                        log.info("Handle exception", e);
                        // TODO 定义默认
                        session.error().code(Errors.Code.Handle_Error_VALUE).data().send();
                    }
                }
            }
            for (UnknownRouter unknownRouter : unknownRouters) {
                try {
                    unknownRouter.handle(session, module, frame);
                } catch (Throwable e) {
                    log.info("Handle exception", e);
                    // TODO 定义默认
                    session.error().code(Errors.Code.Global_Handle_Error_VALUE).data().send();
                }
            }
        });
    }
}
