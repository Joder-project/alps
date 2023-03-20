package org.alps.core;

/**
 * 数据包包装类
 */
public class AlpsPacketWrapper {

    private final AlpsDataCoderFactory dataCoderFactory;
    private final AlpsPacket protocol;
    private volatile short module;
    private volatile AlpsMetadata metadata;
    private volatile AlpsData data;


    public AlpsPacketWrapper(AlpsDataCoderFactory dataCoderFactory, AlpsPacket protocol) {
        this.dataCoderFactory = dataCoderFactory;
        this.protocol = protocol;
        this.module = protocol.module();
        metadata();
        data();
    }

    public AlpsPacketWrapper(short module, AlpsDataCoderFactory dataCoderFactory, AlpsMetadata metadata, AlpsData data) {
        this.dataCoderFactory = dataCoderFactory;
        this.protocol = null;
        this.metadata = metadata;
        this.data = data;
        this.module = module;
    }

    public short module() {
        return module;
    }

    public AlpsMetadata metadata() {
        if (metadata == null && protocol != null) {
            synchronized (this) {
                if (metadata == null) {
                    metadata = AlpsMetadata.create(protocol.metadata(), dataCoderFactory);
                }
            }
        } else if (metadata == null && protocol == null) {
            throw new IllegalStateException();
        }
        return metadata;
    }

    public AlpsData data() {
        if (data == null && protocol != null) {
            synchronized (this) {
                if (data == null) {
                    data = AlpsData.create(protocol.data(), dataCoderFactory);
                }
            }
        } else if (data == null && protocol == null) {
            throw new IllegalStateException();
        }
        return data;
    }

    public AlpsPacket newProtocol() {
        var metadataBytes = metadata().toBytes();
        var dataBytes = data().toBytes();
        return AlpsPacket.create(module, metadataBytes, dataBytes);
    }
}
