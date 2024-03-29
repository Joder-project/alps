package org.alps.core;

/**
 * 数据解码器，定义数据包中的数据解析方式
 */
public interface AlpsDataCoder {

    <T> T encode(byte[] data, int offset, int size, Class<T> clazz) throws Exception;

    default <T> T encode(byte[] data, Class<T> clazz) throws Exception {
        return encode(data, 0, data.length, clazz);
    }

    byte[] decode(Object obj) throws Exception;
}
