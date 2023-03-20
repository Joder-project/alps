package org.alps.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsSession;
import org.alps.core.Frame;
import org.alps.core.FrameListener;

@Slf4j
public class IdleFrameListener implements FrameListener {

    @Override
    public void listen(AlpsSession session, Frame frame) {
        log.debug("Received idle frame. Address: {}", session.targetAddress().getHostAddress());
    }
}
