package org.alps.core.common;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GZipHelper {

    public static byte[] zip(byte[] data) {
        int offset = 0;
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        byte[] ret = null;
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var gzip = new GZIPOutputStream(byteArrayOutputStream)) {
            gzip.write(data, offset, data.length - offset);
            ret = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new AlpsException("压缩metadata失败", e);
        }

        return ret;
    }

    public static byte[] unzip(byte[] data, int offset) {
        if (data == null || data.length == 0 || data.length - offset == 0) {
            return new byte[0];
        }

        byte[] ret;
        try (var out = new ByteArrayOutputStream();
             var in = new ByteArrayInputStream(data, offset, data.length - offset);
             GZIPInputStream gzip = new GZIPInputStream(in);) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            ret = out.toByteArray();
        } catch (IOException e) {
            throw new AlpsException("解压metadata失败", e);
        }

        return ret;
    }
}
