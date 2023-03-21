package org.alps.starter;

import org.alps.core.AlpsClient;
import org.alps.core.AlpsEnhancedSession;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientSessionManager {
    private final AlpsClient client;
    private final Map<String, Short> moduleMap;


    public ClientSessionManager(AlpsClient client, AlpsProperties properties) {
        this.client = client;
        this.moduleMap = properties.getModules()
                .stream()
                .collect(Collectors.toMap(AlpsProperties.ModuleProperties::getName, AlpsProperties.ModuleProperties::getCode));
    }

    public Optional<SpringAlpsSession> session(String module) {
        if (moduleMap.containsKey(module)) {
            return client.session(moduleMap.get(module)).map(e -> new SpringAlpsSession(((AlpsEnhancedSession) e)));
        }
        return Optional.empty();
    }
}
