package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

import java.util.Optional;

/**
 * 帧类型
 */
public interface Frame {

    int IDLE = 0;
    int FNF = 1;
    int REQUEST = 2;
    int RESPONSE = 3;
    int ERROR = 4;

    AlpsMetadata metadata();

    AlpsData data();

    Optional<AlpsProtocol.AlpsPacket> rawPacket();

    /**
     * 字节流
     *
     * @return
     */
    byte[] toBytes();
}

