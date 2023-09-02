package org.alps.core.listener;

import org.alps.core.*;

public class CommandFrameListener implements FrameListener {

    private final RouterDispatcher routerDispatcher;

    public CommandFrameListener(RouterDispatcher routerDispatcher) {
        this.routerDispatcher = routerDispatcher;
    }

    @Override
    public void listen(AlpsSession session, Frame frame) {
        routerDispatcher.dispatch(((AlpsEnhancedSession) session), ((CommandFrame) frame));
    }
}
