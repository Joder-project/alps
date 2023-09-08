package org.alps.core;

import org.alps.core.common.AlpsException;
import org.alps.core.frame.*;
import org.alps.core.proto.AlpsProtocol.AlpsPacket.FrameType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.alps.core.FrameCoders.DefaultFrame.*;


public class FrameCoders {

    private final Map<Byte, FrameCoder> defaultCoders = Stream.of(
            IDLE, FORGET, REQUEST, RESPONSE, ERROR, STREAM_REQUEST, STREAM_RESPONSE, MODULE_AUTH
    ).collect(Collectors.toUnmodifiableMap(e -> e.frameType, e -> e.coder));
    private final Map<Byte, FrameCoder> customCoder = new HashMap<>(16);

    public FrameCoders() {

    }

    public Frame decode(AlpsPacket protocol) throws Exception {
        var frameType = protocol.metadata().frameType();
        if (defaultCoders.containsKey(frameType)) {
            return defaultCoders.get(frameType).decode(protocol.metadata(), protocol.data(), protocol.rawPacket());
        } else if (customCoder.containsKey(frameType)) {
            return customCoder.get(frameType).decode(protocol.metadata(), protocol.data(), protocol.rawPacket());
        }
        throw new AlpsException(String.format("不存在解码器(code=%d)", frameType));
    }

    public AlpsPacket encode(int socketType, String module, Frame frame) {
        Objects.requireNonNull(frame, "frame不能为空");
        return getFrameType(frame).encode(socketType, module, frame);
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
        IDLE(IdleFrame.class, (byte) FrameType.IDLE_VALUE, new IdleFrame.Coder()),
        FORGET(ForgetFrame.class, (byte) FrameType.FORGET_VALUE, new ForgetFrame.Coder()),
        REQUEST(RequestFrame.class, (byte) FrameType.REQUEST_VALUE, new RequestFrame.Coder()),
        RESPONSE(ResponseFrame.class, (byte) FrameType.RESPONSE_VALUE, new ResponseFrame.Coder()),
        ERROR(ErrorFrame.class, (byte) FrameType.ERROR_VALUE, new ErrorFrame.Coder()),
        STREAM_REQUEST(StreamRequestFrame.class, (byte) FrameType.STREAM_REQUEST_VALUE, new StreamRequestFrame.Coder()),
        STREAM_RESPONSE(StreamResponseFrame.class, (byte) FrameType.STREAM_RESPONSE_VALUE, new StreamResponseFrame.Coder()),
        MODULE_AUTH(ModuleAuthFrame.class, (byte) FrameType.MODULE_AUTH_VALUE, new ModuleAuthFrame.Coder()),
        ROUTING(RoutingFrame.class, (byte) FrameType.ROUTING_VALUE, new RoutingFrame.Coder()),
        GATEWAY(GatewayFrame.class, (byte) FrameType.GATEWAY_T_VALUE, new GatewayFrame.Coder()),
        CUSTOM(CustomFrame.class, (byte) FrameType.CUSTOM_VALUE, new CustomFrame.Coder()),
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
