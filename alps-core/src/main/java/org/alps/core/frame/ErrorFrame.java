package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record ErrorFrame(
        short code,
        AlpsMetadata metadata,
        AlpsData data,
        AlpsProtocol.AlpsPacket packet
) implements Frame {

    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }

    public static byte[] toBytes(short code) {
        return new ErrorFrame(code, null, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.ErrorFrame.newBuilder()
                .setCode(code)
                .build().toByteArray();
    }

    public static class Coder implements FrameCoder {


        @Override
        public Class<? extends Frame> target() {
            return ErrorFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var frame = metadata.frame();
            var errorFrame = IFrame.ErrorFrame.parseFrom(metadata.frame());
            return new ErrorFrame(((short) errorFrame.getCode()), metadata, data,rawPacket);
        }

    }
}
