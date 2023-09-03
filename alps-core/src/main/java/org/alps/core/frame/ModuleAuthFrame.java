package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record ModuleAuthFrame(
        int version,
        long verifyToken,
        AlpsMetadata metadata,
        AlpsProtocol.AlpsPacket packet
) implements Frame {

    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }

    public static byte[] toBytes(int version, long verifyToken) {
        return new ModuleAuthFrame(version, verifyToken, null, null).toBytes();
    }

    @Override
    public AlpsData data() {
        return AlpsData.EMPTY;
    }

    @Override
    public byte[] toBytes() {
        return IFrame.ModuleAuthFrame.newBuilder()
                .setVersion(version)
                .setVerifyToken(verifyToken)
                .build()
                .toByteArray();
    }

    public static class Coder implements FrameCoder {

        @Override
        public Class<? extends Frame> target() {
            return ModuleAuthFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var frame = IFrame.ModuleAuthFrame.parseFrom(metadata.frame());
            return new ModuleAuthFrame(frame.getVersion(), frame.getVerifyToken(), metadata, rawPacket);
        }

    }
}