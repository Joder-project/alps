package org.alps.core.frame;

import org.alps.core.*;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record ForgetFrame(
        int command,
        AlpsMetadata metadata,
        AlpsData data,
        AlpsProtocol.AlpsPacket packet
) implements CommandFrame {

    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }

    public static byte[] toBytes(int command) {
        return new ForgetFrame(command, null, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.ForgetFrame.newBuilder()
                .setCommand(command)
                .build().toByteArray();
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return ForgetFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var forgetFrame = IFrame.ForgetFrame.parseFrom(metadata.frame());
            return new ForgetFrame(forgetFrame.getCommand(), metadata, data, rawPacket);
        }

    }
}
