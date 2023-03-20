package org.alps.core;

import java.util.concurrent.CompletableFuture;

/**
 * 无论是服务端, 还是客户端都是socket
 */
public interface AlpsSocket {

    /**
     * 启动服务器
     */
    void start();

    /**
     * 关闭服务器
     *
     * @return
     */
    CompletableFuture<Void> close();

    void addSession(AlpsSession session);

    void removeSession(AlpsSession session);
}
