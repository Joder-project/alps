package org.alps.core;

import org.alps.core.common.GZipHelper;
import org.alps.core.common.NumberHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 协议中的数据
 * |isZip(1) | dataSize(15) | dataCoder(8) | data[0].length | data[0] | ... |
 *
 * @param isZip     是否压缩
 * @param coder     数据的解码器
 * @param dataArray 数据
 */
public record AlpsData(
        boolean isZip,
        byte dataCoder,
        AlpsDataCoder coder,
        InnerValue[] dataArray
) {

    public static final AlpsData EMPTY = new AlpsData(false, (byte) 0, null, new InnerValue[0]);

    public static AlpsData create(byte[] data, AlpsDataCoderFactory coderFactory) {
        if (data == null || data.length == 0) {
            return EMPTY;
        }
        boolean isZip = (data[0] & 0x80) > 0;
        short dataSize = (short) (NumberHelper.readShort(data, 0) & 0x7FFF);
        byte dataCoder = data[2];
        var coder = coderFactory.getCoder(dataCoder);
        InnerValue[] dataArray;
        if (isZip && dataSize > 0) {
            dataArray = parseData(dataSize, GZipHelper.unzip(data, 3), 0, coder);
        } else {
            dataArray = parseData(dataSize, data, 3, coder);
        }
        return new AlpsData(isZip, dataCoder, coder, dataArray);
    }

    static InnerValue[] parseData(int size, byte[] data, int offset, AlpsDataCoder coder) {
        InnerValue[] ret = new InnerValue[size];
        for (int i = 0; i < size; i++) {
            var len = NumberHelper.readInt(data, offset);
            offset += 4;
            if (len > 0) {
                var d = Arrays.copyOfRange(data, offset, offset + len);
                ret[i] = new InnerValue(coder, d);
                offset += len;
            } else {
                ret[i] = new InnerValue(coder, new byte[0]);
            }

        }
        return ret;
    }

    public byte[] toBytes() {
        var dataToBytes = dataToBytes();
        if (isZip && dataArray.length > 0) {
            dataToBytes = GZipHelper.zip(dataToBytes);
        }
        var data = new byte[3 + dataToBytes.length];
        int offset = 0;
        if (isZip) {
            data[offset++] = (byte) ((dataArray.length >> 8) | 0x80);
            data[offset++] = (byte) (dataArray.length & 0xFF);
        } else {
            data[offset++] = (byte) ((dataArray.length >> 8));
            data[offset++] = (byte) (dataArray.length & 0xFF);
        }
        data[offset++] = dataCoder;
        System.arraycopy(dataToBytes, 0, data, offset, dataToBytes.length);

        return data;
    }

    byte[] dataToBytes() {
        byte[][] data = new byte[dataArray.length][];
        int index = 0;
        int size = 0;
        for (var entry : dataArray) {

            var value = entry.data();
            if (value.length == 0) {
                data[index] = new byte[4];
            } else {
                data[index] = new byte[4 + value.length];
            }
            size += data[index].length;
            NumberHelper.writeInt(value.length, data[index], 0);
            if (value.length > 0) {
                System.arraycopy(value, 0, data[index], 4, value.length);
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

}

class AlpsDataBuilder {

    private boolean isZip;
    private byte dataCoder;
    private AlpsDataCoder coder;
    private final List<Object> dataArray = new ArrayList<>(4);

    public AlpsDataBuilder isZip(boolean isZip) {
        this.isZip = isZip;
        return this;
    }

    public AlpsDataBuilder dataCoder(byte dataCoder) {
        this.dataCoder = dataCoder;
        return this;
    }

    public AlpsDataBuilder coder(AlpsDataCoder coder) {
        this.coder = coder;
        return this;
    }

    public AlpsDataBuilder addData(Object... value) {
        dataArray.addAll(Arrays.asList(value));
        return this;
    }

    public AlpsData build() {
        Objects.requireNonNull(coder, "coder不能为空");
        var values = new InnerValue[dataArray.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = new InnerValue(coder, dataArray.get(i));
        }
        return new AlpsData(isZip, dataCoder, coder, values);
    }
}