package org.alps.starter;

import org.alps.core.AlpsClient;
import org.alps.core.AlpsEnhancedSession;

import java.util.Optional;

public class ClientSessionManager {
    private final AlpsClient client;


    public ClientSessionManager(AlpsClient client) {
        this.client = client;
    }

    public Optional<SpringAlpsSession> session(String module) {
        return client.session(module).map(e -> new SpringAlpsSession(((AlpsEnhancedSession) e)));
    }
}
