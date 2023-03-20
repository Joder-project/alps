package org.alps.core;

import org.alps.core.common.AlpsException;
import org.alps.core.frame.*;

import java.util.*;
import java.util.stream.Stream;

import static org.alps.core.FrameCoders.DefaultFrame.*;


public class FrameCoders {

    private final List<FrameCoder> defaultCoders = Stream.of(
            IDLE, FORGET, REQUEST, RESPONSE
    ).sorted(Comparator.comparingInt(x -> x.frameType)).map(e -> e.coder).toList();
    private final Map<Byte, FrameCoder> customCoder = new HashMap<>(16);

    private final AlpsDataCoderFactory dataCoderFactory;

    public FrameCoders(AlpsDataCoderFactory dataCoderFactory) {
        this.dataCoderFactory = dataCoderFactory;
    }

    public Frame decode(AlpsPacketWrapper protocol) {
        var frameType = protocol.metadata().frameType();
        if (frameType >= 0 && frameType < defaultCoders.size()) {
            return defaultCoders.get(frameType).decode(protocol.metadata(), protocol.data());
        } else if (customCoder.containsKey(frameType)) {
            return customCoder.get(frameType).decode(protocol.metadata(), protocol.data());
        }
        throw new AlpsException(String.format("不存在解码器(code=%d)", frameType));
    }

    public AlpsPacketWrapper encode(short module, Frame frame) {
        Objects.requireNonNull(frame, "frame不能为空");
        return getFrameType(frame).encode(module, dataCoderFactory, frame);
    }

    FrameCoder getFrameType(Frame frame) {
        DefaultFrame defaultFrame = valueOf(frame.getClass());
        if (defaultFrame == null) {
            return customCoder.values()
                    .stream()
                    .filter(e -> e.target().equals(frame.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new AlpsException(String.format("不存在解码器(class=%s)", frame.getClass().getName())));
        }
        return defaultFrame.coder;
    }

    /**
     * 添加自定义解码器
     *
     * @param code  协议中代号
     * @param coder 解码器
     */
    public void addCoder(byte code, FrameCoder coder) {
        if (code < 32) {
            throw new AlpsException("不能添加code小于32的解码器");
        } else if (customCoder.containsKey(code)) {
            throw new AlpsException(String.format("解码器 (code=%d) 已存在", code));
        }
        Objects.requireNonNull(coder, "解码器不能为空");
        customCoder.put(code, coder);
    }

    enum DefaultFrame {
        IDLE(IdleFrame.class, (byte) 0, new IdleFrame.Coder()),
        FORGET(ForgetFrame.class, (byte) 1, new ForgetFrame.Coder()),
        REQUEST(RequestFrame.class, (byte) 2, new RequestFrame.Coder()),
        RESPONSE(ResponseFrame.class, (byte) 3, new ResponseFrame.Coder()),
        ERROR(ErrorFrame.class, (byte) 4, new ErrorFrame.Coder()),
        ;


        final Class<? extends Frame> frameClass;
        final byte frameType;
        final FrameCoder coder;

        DefaultFrame(Class<? extends Frame> frameClass, byte frameType, FrameCoder coder) {
            this.frameClass = frameClass;
            this.frameType = frameType;
            this.coder = coder;
        }

        static DefaultFrame valueOf(Class<? extends Frame> frameClass) {
            for (DefaultFrame value : values()) {
                if (value.frameClass.equals(frameClass)) {
                    return value;
                }
            }
            return null;
        }
    }
}
