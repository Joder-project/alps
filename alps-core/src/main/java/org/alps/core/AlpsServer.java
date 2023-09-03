package org.alps.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 服务端
 */
public interface AlpsServer extends AlpsSocket {

    List<AlpsSession> sessions();

    default List<AlpsSession> sessions(String module) {
        return sessions().stream()
                .filter(e -> Objects.equals(e.module(), module))
                .toList();
    }

    default Optional<AlpsSession> session(String module, Predicate<AlpsSession> predicate) {
        return sessions(module).stream()
                .filter(predicate)
                .findFirst();
    }

}
