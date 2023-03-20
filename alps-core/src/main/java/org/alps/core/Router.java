package org.alps.core;

/**
 * 对指令数据进行处理
 */
public interface Router {

    /**
     * 处理模块
     *
     * @return
     */
    short module();

    /**
     * 处理指令
     *
     * @return
     */
    int command();

    void handle(AlpsEnhancedSession session, CommandFrame frame);
}
