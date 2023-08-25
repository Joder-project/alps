package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.IFrame;

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
        return IFrame.ErrorFrame.newBuilder()
                .setId(id)
                .setCode(code)
                .build().toByteArray();
    }

    public static class Coder implements FrameCoder {


        @Override
        public Class<? extends Frame> target() {
            return ErrorFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) throws Exception {
            var frame = metadata.frame();
            var errorFrame = IFrame.ErrorFrame.parseFrom(metadata.frame());
            return new ErrorFrame(errorFrame.getId(), ((short) errorFrame.getCode()), metadata, data);
        }

    }
}
