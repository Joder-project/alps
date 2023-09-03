package org.alps.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsSession;
import org.alps.core.Frame;
import org.alps.core.FrameListener;
import org.alps.core.frame.ErrorFrame;

@Slf4j
public class ErrorFrameListener implements FrameListener {

    @Override
    public void listen(AlpsSession session, Frame frame) {
        log.error("Received error frame. code: {}, Address: {}", ((ErrorFrame) frame).code(),
                session.targetAddress().orElse(""));
    }
}
