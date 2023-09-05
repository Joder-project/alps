package org.alps.core.frame;

import com.google.protobuf.ByteString;
import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record CustomFrame(
        byte[] frameData,
        AlpsMetadata metadata,
        AlpsData data,
        AlpsProtocol.AlpsPacket packet
) implements Frame {

    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }

    public static byte[] toBytes(byte[] frameData) {
        return new CustomFrame(frameData, null, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.CustomFrame.newBuilder()
                .setData(ByteString.copyFrom(frameData))
                .build()
                .toByteArray();
    }

    public static class Coder implements FrameCoder {

        @Override
        public Class<? extends Frame> target() {
            return CustomFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var frame = IFrame.CustomFrame.parseFrom(metadata.frame());
            return new CustomFrame(frame.getData().toByteArray(), metadata, data, rawPacket);
        }

    }
}