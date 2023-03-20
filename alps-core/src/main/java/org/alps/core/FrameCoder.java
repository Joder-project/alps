package org.alps.core;

/**
 * 帧解码器
 */
public interface FrameCoder {

    Class<? extends Frame> target();

    Frame decode(AlpsMetadata metadata, AlpsData data);

    default AlpsPacketWrapper encode(AlpsDataCoderFactory dataCoderFactory, Frame frame) {
        return encode(AlpsPacket.ZERO_MODULE, dataCoderFactory, frame);
    }

    default AlpsPacketWrapper encode(short module, AlpsDataCoderFactory dataCoderFactory, Frame frame) {
        return new AlpsPacketWrapper(module, dataCoderFactory, frame.metadata(), frame.data());
    }

}
