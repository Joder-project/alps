package org.alps.core.support;

import org.alps.core.AlpsDataCoder;
import org.alps.core.AlpsMetadata;
import org.alps.core.InnerValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AlpsMetadataBuilder {

    private boolean isZip;
    private byte frameType;
    private byte[] frame;
    private byte containerCoder;
    private final Map<String, Object> metadata = new HashMap<>();
    private AlpsDataCoder coder;

    public AlpsMetadataBuilder isZip(boolean isZip) {
        this.isZip = isZip;
        return this;
    }

    public AlpsMetadataBuilder frameType(byte frameType) {
        this.frameType = frameType;
        return this;
    }

    public AlpsMetadataBuilder frame(byte[] frame) {
        this.frame = frame;
        return this;
    }

    public AlpsMetadataBuilder containerCoder(byte containerCoder) {
        this.containerCoder = containerCoder;
        return this;
    }

    public AlpsMetadataBuilder coder(AlpsDataCoder coder) {
        this.coder = coder;
        return this;
    }

    public AlpsMetadataBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public AlpsMetadata build() {
        Objects.requireNonNull(coder, "coder不能为空");
        var map = new HashMap<String, InnerValue>(metadata.size());
        metadata.forEach((k, v) -> map.put(k, new InnerValue(coder, v)));
        return new AlpsMetadata(isZip, frameType, frame, containerCoder, Collections.unmodifiableMap(map), coder);
    }
}
