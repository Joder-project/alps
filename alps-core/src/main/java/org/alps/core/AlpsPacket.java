package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

/**
 * 数据包包装类
 */
public record AlpsPacket(int connectType, String module, AlpsMetadata metadata, AlpsData data,
                         AlpsProtocol.AlpsPacket rawPacket) {
    public static final String ZERO_MODULE = "AlpsDefault";

}
