package org.alps.core;

/**
 * 指令帧的过滤器
 */
public interface RouterFilter {

    /**
     * 判断指令处理是否继续执行
     *
     * @param session
     * @param frame
     * @return true: 继续执行； false: 放弃处理
     */
    boolean filter(AlpsEnhancedSession session, CommandFrame frame);
}
