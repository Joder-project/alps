package org.alps.core;

import java.net.InetAddress;

/**
 * 会话定义
 * 客户端 <=== （会话） ===> 服务端
 */
public interface AlpsSession {

    short module();

    /**
     * 自身地址
     */
    InetAddress selfAddress();

    /**
     * 对方地址
     */
    InetAddress targetAddress();

    /**
     * 发送数据包
     *
     * @param protocol 数据包
     */
    void send(AlpsPacket protocol);

    void close();

}
