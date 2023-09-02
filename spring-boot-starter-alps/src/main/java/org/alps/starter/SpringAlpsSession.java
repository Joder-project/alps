package org.alps.starter;

import org.alps.core.AlpsEnhancedSession;

public record SpringAlpsSession(AlpsEnhancedSession session) {

    public AlpsEnhancedSession.ForgetCommand forget(int command) {
        return session.forget(command);
    }

    public AlpsEnhancedSession.RequestCommand request(int command) {
        return session.request(command);
    }

    public AlpsEnhancedSession.StreamRequestCommand stream(int command) {
        return session.streamRequest(command);
    }
}
