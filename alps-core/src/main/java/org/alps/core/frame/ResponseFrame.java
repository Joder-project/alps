package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.Frame;
import org.alps.core.FrameCoder;
import org.alps.core.proto.IFrame;

public record ResponseFrame(
        int id,
        int reqId,
        AlpsMetadata metadata,
        AlpsData data
) implements Frame {

    public static byte[] toBytes(int id, int reqId) {
        return new ResponseFrame(id, reqId, null, null).toBytes();
    }

    @Override
    public byte[] toBytes() {
        return IFrame.ResponseFrame.newBuilder()
                .setId(id)
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
        public Frame decode(AlpsMetadata metadata, AlpsData data) throws Exception {
            var responseFrame = IFrame.ResponseFrame.parseFrom(metadata.frame());
            return new ResponseFrame(responseFrame.getId(), responseFrame.getReqId(), metadata, data);
        }

    }
}