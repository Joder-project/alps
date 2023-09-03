package org.alps.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.*;
import org.alps.core.proto.Errors;

@Slf4j
public class CommandFrameListener implements FrameListener {

    private final RouterDispatcher routerDispatcher;

    public CommandFrameListener(RouterDispatcher routerDispatcher) {
        this.routerDispatcher = routerDispatcher;
    }

    @Override
    public void listen(AlpsSession session, Frame frame) {
        var enhancedSession = (AlpsEnhancedSession) session;
        if (!enhancedSession.isAuth()) {
            log.error("收到为认证Session请求, modele: {}, address: {}", session.module(), session.targetAddress().orElse(""));
            enhancedSession.error().code(Errors.Code.Module_Reject_Access_VALUE).send().subscribe();
            return;
        }
        routerDispatcher.dispatch(enhancedSession, ((CommandFrame) frame));
    }
}
