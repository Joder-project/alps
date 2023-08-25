package org.alps.core;

import java.util.*;

/**
 * 数据包元数据
 * | isZip(1) | version(15) | verifyToken(64) | frameType(8) | frameLen(16) | frame(frameLen) |
 * | containerCoder(4) | containerSize(12) | isKey(1) | keyLen(15) | key(keyLen) | isKey(1) | valueLen(31) | value(valueLen) |
 *
 * @param isZip
 * @param version     模块版本
 * @param verifyToken 协议对应密钥
 * @param frame       帧数据
 * @param container
 */
public record AlpsMetadata(
        boolean isZip,
        short version,
        long verifyToken, // TODO: 支持module token验证
        byte frameType,
        byte[] frame,
        byte containerCoder,
        Map<String, InnerValue> container,
        AlpsDataCoder coder
) {

    public Optional<Object> getValue(String key, Class<?> clazz) {
        return Optional.ofNullable(container.get(key)).map(e -> e.object(clazz));
    }
}

class AlpsMetadataBuilder {

    private boolean isZip;
    private short version;
    private long verifyToken;
    private byte frameType;
    private byte[] frame;
    private byte containerCoder;
    private final Map<String, Object> metadata = new HashMap<>();
    private AlpsDataCoder coder;

    public AlpsMetadataBuilder isZip(boolean isZip) {
        this.isZip = isZip;
        return this;
    }

    public AlpsMetadataBuilder version(short version) {
        this.version = version;
        return this;
    }

    public AlpsMetadataBuilder verifyToken(long verifyToken) {
        this.verifyToken = verifyToken;
        return this;
    }

    public AlpsMetadataBuilder frameType(byte frameType) {
        this.frameType = frameType;
        return this;
    }

    public AlpsMetadataBuilder frame(byte[] frame) {
        this.frame = frame;
        return this;
    }

    public AlpsMetadataBuilder containerCoder(byte containerCoder) {
        this.containerCoder = containerCoder;
        return this;
    }

    public AlpsMetadataBuilder coder(AlpsDataCoder coder) {
        this.coder = coder;
        return this;
    }

    public AlpsMetadataBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public AlpsMetadata build() {
        Objects.requireNonNull(coder, "coder不能为空");
        var map = new HashMap<String, InnerValue>(metadata.size());
        metadata.forEach((k, v) -> map.put(k, new InnerValue(coder, v)));
        return new AlpsMetadata(isZip, version, verifyToken, frameType, frame, containerCoder, Collections.unmodifiableMap(map), coder);
    }
}
