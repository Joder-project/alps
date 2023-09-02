package org.alps.core.frame;

import org.alps.core.*;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record RequestFrame(
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
        return new RequestFrame(id, command, null, null, null).toBytes();
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
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var frame = IFrame.RequestFrame.parseFrom(metadata.frame());
            return new RequestFrame(frame.getId(), frame.getCommand(), metadata, data, rawPacket);
        }

    }
}