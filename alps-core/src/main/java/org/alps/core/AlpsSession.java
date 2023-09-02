package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

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

    void send(AlpsProtocol.AlpsPacket protocol);

    void close();

    boolean isClose();

    /**
     * 获取session的内存属性
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T attr(String key);

    /**
     * 为session设置内存属性
     *
     * @param key
     * @param value
     * @return
     */
    default AlpsSession attr(String key, Object value) {
        return this;
    }

}
