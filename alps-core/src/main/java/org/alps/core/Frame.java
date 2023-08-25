package org.alps.core;

/**
 * 帧类型
 */
public interface Frame {

    int IDLE = 0;
    int FNF = 1;
    int REQUEST = 2;
    int RESPONSE = 3;
    int ERROR = 4;

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

