package org.alps.core;

public interface SessionListener {

    /**
     * 模块进行连接，不保证是否认证
     */
    void connect(AlpsSession session);

    /**
     * 模块删除连接
     * @param session
     */
    void disconnect(AlpsSession session);
}
