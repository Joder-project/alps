package org.alps.core;

import org.alps.core.common.AssertHelper;
import org.alps.core.common.GZipHelper;
import org.alps.core.common.NumberHelper;

import java.nio.charset.StandardCharsets;
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

    public static AlpsMetadata create(byte[] metadata, AlpsDataCoderFactory coderFactory) {
        AssertHelper.assertTrue(metadata != null && metadata.length > 13, "metadata长度过短");
        boolean isZip = (metadata[0] & 0x80) > 0;
        short version = (short) (NumberHelper.readShort(metadata, 0) & 0x7FFF);
        long verifyToken = NumberHelper.readLong(metadata, 2);
        byte frameType = metadata[10];
        short frameLen = NumberHelper.readShort(metadata, 11);
        byte[] frame = Arrays.copyOfRange(metadata, 13, 13 + frameLen);
        int offset = 13 + frameLen;
        byte containerCoder = (byte) ((metadata[offset] >> 4) & 0xF);
        var coder = coderFactory.getCoder(containerCoder);
        Map<String, InnerValue> container;
        short containerSize = (short) (NumberHelper.readShort(metadata, offset) & 0x0FFF);
        offset += 2;
        if (isZip && containerSize > 0) {
            container = parseContainer(coder, containerSize, GZipHelper.unzip(metadata, offset), 0);
        } else {
            container = parseContainer(coder, containerSize, metadata, offset);
        }
        return new AlpsMetadata(isZip, version, verifyToken, frameType, frame, containerCoder, container, coder);
    }

    /**
     * byte to map
     * value可以为空
     *
     * @param coder    解码器
     * @param metadata 数据
     * @param offset   偏移
     * @return map
     */
    static Map<String, InnerValue> parseContainer(AlpsDataCoder coder, int size, byte[] metadata, int offset) {

        Map<String, InnerValue> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            boolean isKey = (metadata[offset] & 0x80) > 0;
            AssertHelper.assertTrue(isKey, "metadata解析失败");
            short keyLen = (short) (NumberHelper.readShort(metadata, offset) & 0x7FFF);
            offset += 2;
            String key = "";
            if (keyLen > 0) {
                key = new String(metadata, offset, keyLen, StandardCharsets.UTF_8);
            }
            offset += keyLen;
            if (offset >= metadata.length) {
                if (i == size - 1) {
                    map.put(key, new InnerValue(coder, new byte[0]));
                    break;
                } else {
                    throw new IllegalStateException();
                }

            }
            boolean isValue = (metadata[offset] & 0x80) == 0;
            if (!isValue) {
                continue;
            }
            int valueLen = NumberHelper.readInt(metadata, offset) & 0x7FFF_FFFF;
            offset += 4;
            if (valueLen == 0) {
                map.put(key, new InnerValue(coder, new byte[0]));
            } else {
                byte[] value = Arrays.copyOfRange(metadata, offset, valueLen + offset);
                map.put(key, new InnerValue(coder, value));
            }
            offset += valueLen;
        }
        return map;
    }

    public byte[] toBytes() {
        var containerToBytes = containerToBytes();
        if (isZip && container.size() > 0) {
            containerToBytes = GZipHelper.zip(containerToBytes);
        }
        var data = new byte[17 + frame.length + containerToBytes.length];
        int offset = 0;
        if (isZip) {
            data[offset++] = (byte) ((version >> 8) | 0x80);
            data[offset++] = (byte) (version & 0xFF);
        } else {
            data[offset++] = (byte) ((version >> 8));
            data[offset++] = (byte) (version & 0xFF);
        }
        NumberHelper.writeLong(verifyToken, data, offset);
        offset += 8;
        data[offset++] = frameType;
        NumberHelper.writeShort((short) frame.length, data, offset);
        offset += 2;
        System.arraycopy(frame, 0, data, offset, frame.length);
        offset += frame.length;
        NumberHelper.writeShort((short) (((containerCoder << 12) & 0xF000) | (container.size() & 0x0FFF)), data, offset);
        offset += 2;
        System.arraycopy(containerToBytes, 0, data, offset, containerToBytes.length);
        return data;
    }

    byte[] containerToBytes() {
        byte[][] data = new byte[container.size()][];
        int index = 0;
        int size = 0;
        for (var entry : container.entrySet()) {
            var key = entry.getKey().getBytes(StandardCharsets.UTF_8);
            var value = entry.getValue().data();
            if (value.length == 0) {
                data[index] = new byte[2 + key.length];
            } else {
                data[index] = new byte[2 + key.length + 4 + value.length];
            }
            size += data[index].length;
            NumberHelper.writeShort((short) (key.length | 0x8000), data[index], 0);
            System.arraycopy(key, 0, data[index], 2, key.length);
            if (value.length > 0) {
                NumberHelper.writeInt(value.length, data[index], 2 + key.length);
                System.arraycopy(value, 0, data[index], 2 + key.length + 4, value.length);
            }
            index++;
        }
        var ret = new byte[size];
        int offset = 0;
        for (byte[] bytes : data) {
            System.arraycopy(bytes, 0, ret, offset, bytes.length);
            offset += bytes.length;
        }
        return ret;
    }


    public Optional<Object> getValue(String key, Class<?> clazz) {
        return Optional.ofNullable(container.get(key)).map(e -> e.object(clazz));
    }

    public void putValue(String key, Object value) {
        container.put(Objects.requireNonNull(key), new InnerValue(coder, value));
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
        return new AlpsMetadata(isZip, version, verifyToken, frameType, frame, containerCoder, map, coder);
    }
}
