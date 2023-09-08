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

