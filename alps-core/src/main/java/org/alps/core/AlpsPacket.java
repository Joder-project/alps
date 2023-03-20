package org.alps.core;

/**
 * 数据包定义
 * TODO: ssl?
 *
 * @param magic          (8 bit) 模数
 * @param module         模块，保留 0 模块,自定义模块大于 0
 * @param hasExtMetadata (1 bit) 是否拓展头部
 * @param metadataSize   (15 bit or 31 bit) 头部长度
 * @param metadata       header数据
 * @param dataSize       (32 bit) 数据长度
 * @param data           数据
 */
public record AlpsPacket(
        byte magic,
        short module,
        boolean hasExtMetadata,
        int metadataSize,
        byte[] metadata,
        boolean hasData,
        int dataSize,
        byte[] data
) {
    public static final byte MAGIC_NUM = Byte.parseByte(System.getProperty("org.alps.magic", "73"));
    public static final short ZERO_MODULE = 0;

    public static AlpsPacket create(short module, byte[] metadata, byte[] data) {
        boolean hasExtMetadata = metadata.length > 0x7FFF;
        int metadataSize = metadata.length;
        int dataSize = data.length;
        boolean hasData = data.length > 0;
        return new AlpsPacket(MAGIC_NUM, module, hasExtMetadata, metadataSize, metadata, hasData, dataSize, data);
    }
}
