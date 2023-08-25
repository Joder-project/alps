package org.alps.core;

/**
 * 数据包包装类
 */
public record AlpsPacket(short module, AlpsDataCoderFactory dataCoderFactory, AlpsMetadata metadata, AlpsData data) {
    public static final byte MAGIC_NUM = Byte.parseByte(System.getProperty("org.alps.magic", "73"));
    public static final short ZERO_MODULE = 0;

}
