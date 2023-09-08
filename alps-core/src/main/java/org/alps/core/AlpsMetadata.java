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

