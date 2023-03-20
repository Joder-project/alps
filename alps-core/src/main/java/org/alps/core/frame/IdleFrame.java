package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.common.NumberHelper;

public record IdleFrame(
        int id,
        AlpsMetadata metadata
) implements Frame {

    public static byte[] toBytes(int id) {
        return new IdleFrame(id, null).toBytes();
    }

    @Override
    public AlpsData data() {
        return AlpsData.EMPTY;
    }

    @Override
    public byte[] toBytes() {
        var data = new byte[4];
        NumberHelper.writeInt(id, data, 0);
        return data;
    }

    public static class Coder implements FrameCoder {

        @Override
        public Class<? extends Frame> target() {
            return IdleFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) {
            var frame = metadata.frame();
            var id = NumberHelper.readInt(frame, 0);
            return new IdleFrame(id, metadata);
        }

    }
}