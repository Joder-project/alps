package org.alps.core;

import java.util.concurrent.CompletableFuture;

/**
 * 增强型服务端
 */
public class AlpsEnhancedServer {

    private final AlpsServer server;

    public AlpsEnhancedServer(AlpsServer server) {
        this.server = server;
    }

    public void start() {
        this.server.start();
    }


    public CompletableFuture<Void> close() {
        return this.server.close();
    }

    /**
     * 不期待回应
     *
     * @param session 对端
     * @param command 对于指令
     * @return
     */
    public AlpsEnhancedSession.ForgetCommand forget(AlpsEnhancedSession session, int command) {
        return session.forget(command);
    }

    /**
     * 请求响应
     *
     * @param session 对端
     * @param command 指令
     * @return
     */
    public AlpsEnhancedSession.RequestCommand request(AlpsEnhancedSession session, int command) {
        return session.request(command);
    }

}


