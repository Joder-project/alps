package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

/**
 * 帧解码器
 */
public interface FrameCoder {

    Class<? extends Frame> target();

    Frame decode(AlpsMetadata metadata, AlpsData data, AlpsProtocol.AlpsPacket rawPacket) throws Exception;

    default AlpsPacket encode(int socketType, Frame frame) {
        return encode(socketType, AlpsPacket.ZERO_MODULE,frame);
    }

    default AlpsPacket encode(int socketType, String module, Frame frame) {
        return new AlpsPacket(socketType,
                module, frame.metadata(), frame.data(), frame.rawPacket().orElse(null));
    }

}
