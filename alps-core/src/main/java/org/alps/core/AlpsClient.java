package org.alps.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface AlpsClient extends AlpsSocket {

    /**
     * 获取所有连接
     */
    List<AlpsSession> session();

    default Optional<AlpsSession> session(String module) {
        return session().stream()
                .filter(e -> Objects.equals(e.module(), module))
                .findFirst();
    }
}
