package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

import java.util.Optional;

/**
 * 会话定义
 * 客户端 <=== （会话） ===> 服务端
 */
public interface AlpsSession {

    String module();

    /**
     * 自身地址
     */
    Optional<String> selfAddress();

    /**
     * 对方地址
     * TODO Quic Address
     */
    Optional<String> targetAddress();

    /**
     * 发送数据包
     *
     * @param protocol 数据包
     */
    void send(AlpsPacket protocol);

    void send(AlpsProtocol.AlpsPacket protocol);

    void send(byte[] data);

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
