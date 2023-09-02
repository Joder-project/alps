package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.AlpsProtocol;
import org.alps.core.proto.IFrame;

import java.util.Optional;

public record ResponseFrame(
        int reqId,
        AlpsMetadata metadata,
        AlpsData data,
        AlpsProtocol.AlpsPacket packet
) implements Frame {

    @Override
    public Optional<AlpsProtocol.AlpsPacket> rawPacket() {
        return Optional.ofNullable(packet);
    }

    public static byte[] toBytes(int reqId) {
        return new ResponseFrame(reqId, null, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.ResponseFrame.newBuilder()
                .setReqId(reqId)
                .build()
                .toByteArray();
    }

    public static class Coder implements FrameCoder {
        @Override
        public Class<? extends Frame> target() {
            return ResponseFrame.class;
        }

        @Override
        public Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception {
            var responseFrame = IFrame.ResponseFrame.parseFrom(metadata.frame());
            return new ResponseFrame(responseFrame.getReqId(), metadata, data, rawPacket);
        }

    }
}