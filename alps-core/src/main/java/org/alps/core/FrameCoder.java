package org.alps.core;

/**
 * 帧解码器
 */
public interface FrameCoder {

    Class<? extends Frame> target();

    Frame decode(AlpsMetadata metadata, AlpsData data) throws Exception;

    default AlpsPacket encode(AlpsDataCoderFactory dataCoderFactory, Frame frame) {
        return encode(AlpsPacket.ZERO_MODULE, dataCoderFactory, frame);
    }

    default AlpsPacket encode(short module, AlpsDataCoderFactory dataCoderFactory, Frame frame) {
        return new AlpsPacket(module, dataCoderFactory, frame.metadata(), frame.data());
    }

}
