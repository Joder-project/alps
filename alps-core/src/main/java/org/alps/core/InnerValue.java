package org.alps.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * 携带数据包装类
 */
public class InnerValue {
    private final AlpsDataCoder coder;
    private volatile byte[] data;
    private volatile Object object;

    public InnerValue(AlpsDataCoder coder, byte[] data) {
        this.coder = new AlpsDataCoderDecorator(Objects.requireNonNull(coder, "coder不能为空"));
        this.data = data;
    }

    public InnerValue(AlpsDataCoder coder, Object object) {
        this.coder = Objects.requireNonNull(coder, "coder不能为空");
        this.object = object;
    }

    public byte[] data() {
        if (data == null) {
            synchronized (this) {
                if (data == null) {
                    if (object == null) {
                        data = new byte[0];
                    } else {
                        data = coder.decode(object);
                    }
                }
            }
        }
        return data;
    }


    @SuppressWarnings("unchecked")
    public <T> T object(Class<T> clazz) {
        if (object == null) {
            synchronized (this) {
                if (object == null) {
                    if (data == null) {
                        object = null;
                    } else {
                        object = coder.encode(data, clazz);
                    }
                }
            }
        }
        return (T) object;
    }

    @Override
    public int hashCode() {
        if (object != null && data == null) {
            data();
        }
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InnerValue other)) {
            return false;
        }
        if (object != null && other.object != null) {
            return object.equals(other.object);
        } else if (data != null && other.data != null) {
            return Arrays.equals(data, other.data);
        }
        return false;
    }
}
