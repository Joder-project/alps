package org.alps.core;

import java.util.List;
import java.util.Optional;

public interface AlpsClient extends AlpsSocket {

    /**
     * 获取所有连接
     *
     * @return
     */
    List<AlpsSession> session();

    default Optional<AlpsSession> session(short module) {
        return session().stream()
                .filter(e -> e.module() == module)
                .findFirst();
    }
}
