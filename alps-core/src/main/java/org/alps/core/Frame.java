package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

import java.util.Optional;

/**
 * 帧类型
 */
public interface Frame {

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

