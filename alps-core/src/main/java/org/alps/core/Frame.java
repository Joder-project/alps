package org.alps.core;

/**
 * 帧类型
 */
public interface Frame {

    int id();

    AlpsMetadata metadata();

    AlpsData data();

    /**
     * 字节流
     *
     * @return
     */
    byte[] toBytes();
}

