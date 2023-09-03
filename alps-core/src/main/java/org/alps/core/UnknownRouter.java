package org.alps.core;

/**
 * 对未识别通讯进行处理
 */
public interface UnknownRouter {

    void handle(AlpsEnhancedSession session, String module, CommandFrame frame) throws Exception;
}
