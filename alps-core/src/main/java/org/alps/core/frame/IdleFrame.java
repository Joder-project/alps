package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.IFrame;

public record IdleFrame(
        AlpsMetadata metadata
) implements Frame {

    public static byte[] toErrorBytes() {
        return new IdleFrame(null).toBytes();
    }

    @Override
    public AlpsData data() {
        return AlpsData.EMPTY;
    }

    @Override
    public byte[] toBytes() {
        return IFrame.IdleFrame.newBuilder()
                .build()
                .toByteArray();
    }

    public static class Coder implements FrameCoder {

        @Override
        public Class<? extends Frame> target() {
            return IdleFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) throws Exception {
            var idleFrame = IFrame.IdleFrame.parseFrom(metadata.frame());
            return new IdleFrame(metadata);
        }

    }
}