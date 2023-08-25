package org.alps.core.frame;

import org.alps.core.*;
import org.alps.core.proto.IFrame;

public record RequestFrame(
        int id,
        int command,
        AlpsMetadata metadata,
        AlpsData data
) implements CommandFrame {

    public static byte[] toBytes(int command, int id) {
        return new RequestFrame(id, command, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.RequestFrame.newBuilder()
                .setId(id)
                .setCommand(command)
                .build().toByteArray();
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return RequestFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) throws Exception {
            var frame = IFrame.RequestFrame.parseFrom(metadata.frame());
            return new RequestFrame(frame.getId(), frame.getCommand(), metadata, data);
        }

    }
}