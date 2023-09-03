package org.alps.starter;

import org.alps.core.AlpsEnhancedSession;
import org.alps.core.AlpsServer;

import java.util.Set;
import java.util.stream.Collectors;

public class ServerSessionManager {
    private final AlpsServer server;


    public ServerSessionManager(AlpsServer server) {
        this.server = server;
    }

    public Set<SpringAlpsSession> sessions(String module) {
        return server.sessions(module).stream()
                .map(e -> new SpringAlpsSession(((AlpsEnhancedSession) e)))
                .collect(Collectors.toUnmodifiableSet());
    }
}
