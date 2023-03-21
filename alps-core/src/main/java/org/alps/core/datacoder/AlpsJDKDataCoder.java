package org.alps.core.datacoder;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsDataCoder;
import org.alps.core.common.AlpsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Slf4j
public class AlpsJDKDataCoder implements AlpsDataCoder {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T encode(byte[] data, int offset, int size, Class<T> clazz) {
        try (var inputStream = new ByteArrayInputStream(data, offset, size); var in = new ObjectInputStream(inputStream)) {
            return (T) in.readObject();
        } catch (Exception e) {
            log.error("解析失败", e);
            throw new AlpsException(e);
        }
    }

    @Override
    public byte[] decode(Object obj) {
        try (var outputStream = new ByteArrayOutputStream(); var out = new ObjectOutputStream(outputStream);) {
            out.writeObject(obj);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("解析失败", e);
            throw new AlpsException(e);
        }
    }
}
