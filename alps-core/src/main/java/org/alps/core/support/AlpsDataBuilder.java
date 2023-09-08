package org.alps.core.support;

import org.alps.core.AlpsData;
import org.alps.core.AlpsDataCoder;
import org.alps.core.InnerValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AlpsDataBuilder {

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
