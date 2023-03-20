package org.alps.core.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipHelper {

    public static byte[] zip(byte[] data) {
        int offset = 0;
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(byteArrayOutputStream)) {
            gzip.write(data, offset, data.length - offset);
        } catch (IOException e) {
            throw new AlpsException("压缩metadata失败", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] unzip(byte[] data, int offset) {
        if (data == null || data.length == 0 || data.length - offset == 0) {
            return new byte[0];
        }
        var out = new ByteArrayOutputStream();
        var in = new ByteArrayInputStream(data, offset, data.length - offset);
        try {
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new AlpsException("解压metadata失败", e);
        }

        return out.toByteArray();
    }
}
