package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

/**
 * 帧解码器
 */
public interface FrameCoder {

    Class<? extends Frame> target();

    Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception;

    default AlpsPacket encode(AlpsDataCoderFactory dataCoderFactory, Frame frame) {
        return encode(AlpsPacket.ZERO_MODULE, dataCoderFactory, frame);
    }

    default AlpsPacket encode(short module, AlpsDataCoderFactory dataCoderFactory, Frame frame) {
        return new AlpsPacket(module, dataCoderFactory, frame.metadata(), frame.data(), frame.rawPacket().orElse(null));
    }

}
