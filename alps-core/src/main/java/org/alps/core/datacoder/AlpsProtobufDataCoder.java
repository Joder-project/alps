package org.alps.core.datacoder;

import com.google.protobuf.MessageLite;
import org.alps.core.AlpsDataCoder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AlpsProtobufDataCoder implements AlpsDataCoder {

    private final ConcurrentMap<Class<?>, MessageLite> cache = new ConcurrentHashMap<>();

    public AlpsProtobufDataCoder() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T encode(byte[] data, int offset, int size, Class<T> clazz) throws Exception {
        if (!MessageLite.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("不支持非MessageLite类型");
        }
        MessageLite prototype = getDefaultValue(clazz);

        return (T) prototype.getParserForType().parseFrom(data, offset, size);
    }

    @Override
    public byte[] decode(Object obj) throws Exception {
        if (obj instanceof MessageLite messageLite) {
            return messageLite.toByteArray();
        }
        return new byte[0];
    }

    MessageLite getDefaultValue(Class<?> clazz) throws Exception {
        if (!cache.containsKey(clazz)) {
            synchronized (this) {
                if (!cache.containsKey(clazz)) {
                    var constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    MessageLite prototype = (MessageLite) constructor.newInstance();
                    cache.put(clazz, prototype);
                }
            }
        }
        return cache.get(clazz);
    }
}
