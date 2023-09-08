package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

/**
 * 数据包包装类
 */
public record AlpsPacket(int connectType, String module, AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) {
    public static final byte MAGIC_NUM = Byte.parseByte(System.getProperty("org.alps.magic", "73"));
    public static final String ZERO_MODULE = "AlpsDefault";

}
