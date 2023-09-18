package org.alps.core;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SessionListeners {

    private final List<SessionListener> sessionListeners;

    public SessionListeners(List<SessionListener> sessionListeners) {
        this.sessionListeners = sessionListeners;
    }


    void connect(AlpsSession session) {
        for (SessionListener sessionListener : sessionListeners) {
            try {
                sessionListener.connect(session);
            } catch (Exception ex) {
                log.error("sessionListener {} handler error", sessionListener);
            }
        }
    }

    void disconnect(AlpsSession session) {
        for (SessionListener sessionListener : sessionListeners) {
            try {
                sessionListener.disconnect(session);
            } catch (Exception ex) {
                log.error("sessionListener {} handler error", sessionListener);
            }
        }
    }
}
