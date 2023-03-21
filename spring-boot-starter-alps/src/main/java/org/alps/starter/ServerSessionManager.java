package org.alps.starter;

import org.alps.core.AlpsEnhancedSession;
import org.alps.core.AlpsServer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerSessionManager {
    private final AlpsServer server;
    private final Map<String, Short> moduleMap;


    public ServerSessionManager(AlpsServer server, AlpsProperties properties) {
        this.server = server;
        this.moduleMap = properties.getModules()
                .stream()
                .collect(Collectors.toMap(AlpsProperties.ModuleProperties::getName, AlpsProperties.ModuleProperties::getCode));
    }

    public Set<SpringAlpsSession> sessions(String module) {
        if (moduleMap.containsKey(module)) {
            return server.sessions(moduleMap.get(module)).stream()
                    .map(e -> new SpringAlpsSession(((AlpsEnhancedSession) e)))
                    .collect(Collectors.toUnmodifiableSet());
        }
        return Collections.emptySet();
    }
}
