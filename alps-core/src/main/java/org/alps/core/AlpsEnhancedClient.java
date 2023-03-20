package org.alps.core;

import java.util.concurrent.CompletableFuture;

/**
 * 增强型客户端
 */
public class AlpsEnhancedClient {

    private final AlpsClient client;

    public AlpsEnhancedClient(AlpsClient client) {
        this.client = client;
    }

    public void start() {
        this.client.start();
    }


    public CompletableFuture<Void> close() {
        return this.client.close();
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


