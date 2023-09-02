package org.alps.core.frame;

import org.alps.core.*;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record StreamRequestFrame(
        int id,
        int command,
        AlpsMetadata metadata,
        AlpsData data,
        AlpsProtocol.AlpsPacket packet
) implements CommandFrame {
    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }


    public static byte[] toBytes(int command, int id) {
        return new StreamRequestFrame(id, command, null, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.StreamRequestFrame.newBuilder()
                .setId(id)
                .setCommand(command)
                .build().toByteArray();
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return StreamRequestFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var frame = IFrame.StreamRequestFrame.parseFrom(metadata.frame());
            return new StreamRequestFrame(frame.getId(), frame.getCommand(), metadata, data, rawPacket);
        }

    }
}
