package org.alps.core.frame;

import org.alps.core.*;
import org.alps.core.proto.IFrame;

public record ForgetFrame(
        int id,
        int command,
        AlpsMetadata metadata,
        AlpsData data
) implements CommandFrame {

    public static byte[] toBytes(int command, int id) {
        return new ForgetFrame(id, command, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.ForgetFrame.newBuilder()
                .setId(id)
                .setCommand(command)
                .build().toByteArray();
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return ForgetFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) throws Exception {
            var forgetFrame = IFrame.ForgetFrame.parseFrom(metadata.frame());
            return new ForgetFrame(forgetFrame.getId(), forgetFrame.getCommand(), metadata, data);
        }

    }
}
