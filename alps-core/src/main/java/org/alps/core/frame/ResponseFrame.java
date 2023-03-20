package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.common.NumberHelper;

public record ResponseFrame(
        int id,
        int reqId,
        AlpsMetadata metadata,
        AlpsData data
) implements Frame {

    public static byte[] toBytes(int id, int reqId) {
        return new ResponseFrame(id, reqId, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        var data = new byte[8];
        NumberHelper.writeInt(id, data, 0);
        NumberHelper.writeInt(reqId, data, 4);
        return data;
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return ResponseFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) {
            var frame = metadata.frame();
            var id = NumberHelper.readInt(frame, 0);
            var reqId = NumberHelper.readInt(frame, 4);
            return new ResponseFrame(id, reqId, metadata, data);
        }

    }
}