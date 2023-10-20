package org.alps.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.*;

@Slf4j
public class CommandFrameListener implements FrameListener {

    private final RouterDispatcher routerDispatcher;

    public CommandFrameListener(RouterDispatcher routerDispatcher) {
        this.routerDispatcher = routerDispatcher;
    }

    @Override
    public void listen(AlpsSession session, Frame frame) {
        var enhancedSession = (AlpsEnhancedSession) session;
        var commandFrame = (CommandFrame) frame;
        routerDispatcher.dispatch(enhancedSession, commandFrame);
    }
}
