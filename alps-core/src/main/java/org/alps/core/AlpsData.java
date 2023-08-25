package org.alps.core;

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

    public int dataSize() {
        return dataArray.length;
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