package org.alps.core;

import org.alps.core.common.AlpsException;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

/**
 * 携带数据包装类
 */
public class InnerValue {
    private final AlpsDataCoder coder;
    private volatile byte[] data;
    private volatile Object object;

    private final StampedLock stampedLock = new StampedLock();

    public InnerValue(AlpsDataCoder coder, byte[] data) {
        this.coder = Objects.requireNonNull(coder, "coder不能为空");
        this.data = data;
    }

    public InnerValue(AlpsDataCoder coder, Object object) {
        this.coder = Objects.requireNonNull(coder, "coder不能为空");
        this.object = object;
    }

    public byte[] data() {
        if (data == null) {
            var writeLock = stampedLock.writeLock();
            try {
                if (data == null) {
                    if (object == null) {
                        data = new byte[0];
                    } else {
                        try {
                            data = coder.decode(object);
                        } catch (Exception e) {
                            throw new AlpsException("数据转换异常", e);
                        }
                    }
                }
            } finally {
                stampedLock.unlockWrite(writeLock);
            }
        }
        return data;
    }


    @SuppressWarnings("unchecked")
    public <T> T object(Class<T> clazz) {
        if (object == null) {
            var writeLock = stampedLock.writeLock();
            try {
                if (object == null) {
                    if (data == null) {
                        object = null;
                    } else {
                        try {
                            object = coder.encode(data, clazz);
                        } catch (Exception e) {
                            throw new AlpsException("数据转换异常" + clazz.getName(), e);
                        }
                    }
                }
            } finally {
                stampedLock.unlockWrite(writeLock);
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
