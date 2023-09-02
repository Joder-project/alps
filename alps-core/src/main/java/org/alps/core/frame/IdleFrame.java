package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record IdleFrame(
        AlpsMetadata metadata,
        AlpsProtocol.AlpsPacket packet
) implements Frame {

    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }

    public static byte[] toErrorBytes() {
        return new IdleFrame(null, null).toBytes();
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
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var idleFrame = IFrame.IdleFrame.parseFrom(metadata.frame());
            return new IdleFrame(metadata, rawPacket);
        }

    }
}