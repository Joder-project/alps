package org.alps.core;

import org.alps.core.datacoder.AlpsJDKDataCoder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据解析器注册器
 */
public class AlpsDataCoderFactory {

    private final Map<Byte, AlpsDataCoder> coderMap = new ConcurrentHashMap<>();

    public AlpsDataCoderFactory() {
        addCoder((byte) 0, new AlpsJDKDataCoder());
    }

    AlpsDataCoder getCoder(byte code) {
        return coderMap.get(code);
    }

    public void addCoder(Byte code, AlpsDataCoder coder) {
        if (coderMap.containsKey(code)) {
            throw new IllegalArgumentException("Coder already exists for code: " + code);
        }
        coderMap.put(code, Objects.requireNonNull(coder));
    }
}
