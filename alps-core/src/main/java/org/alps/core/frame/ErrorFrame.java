package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.common.NumberHelper;

public record ErrorFrame(
        int id,
        short code,
        AlpsMetadata metadata,
        AlpsData data
) implements Frame {

    public static byte[] toBytes(int id, short code) {
        return new ErrorFrame(id, code, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        var data = new byte[6];
        NumberHelper.writeInt(id, data, 0);
        NumberHelper.writeShort(code, data, 4);
        return data;
    }

    public static class Coder implements FrameCoder {


        @Override
        public Class<? extends Frame> target() {
            return ErrorFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) {
            var frame = metadata.frame();
            var id = NumberHelper.readInt(frame, 0);
            var code = NumberHelper.readShort(frame, 4);
            return new ErrorFrame(id, code, metadata, data);
        }

    }
}
