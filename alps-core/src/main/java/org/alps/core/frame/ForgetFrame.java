package org.alps.core.frame;

import org.alps.core.*;
import org.alps.core.common.NumberHelper;

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
        var data = new byte[8];
        NumberHelper.writeInt(id, data, 0);
        NumberHelper.writeInt(command, data, 4);
        return data;
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return ForgetFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data) {
            var frame = metadata.frame();
            var id = NumberHelper.readInt(frame, 0);
            var command = NumberHelper.readInt(frame, 4);
            return new ForgetFrame(id, command, metadata, data);
        }

    }
}
