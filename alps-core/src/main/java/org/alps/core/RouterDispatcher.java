package org.alps.core;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 协议处理转发
 */
@Slf4j
public class RouterDispatcher {

    private final Map<Short, Map<Integer, Router>> routes = new ConcurrentHashMap<>();
    private final List<RouterFilter> filters = new CopyOnWriteArrayList<>();

    public void addRouter(Router router) {
        routes.computeIfAbsent(router.module(), k -> new ConcurrentHashMap<>()).put(router.command(), router);
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
        short module = session.module();
        int command = frame.command();
        if (routes.containsKey(module)) {
            var routerMap = routes.get(module);
            if (routerMap.containsKey(command)) {
                var router = routerMap.get(command);
                try {
                    router.handle(session, frame);
                } catch (Exception e) {
                    log.info("Handle exception", e);
                    // TODO
//                    session.error().code().data().send();
                }
            }
        }
    }
}
