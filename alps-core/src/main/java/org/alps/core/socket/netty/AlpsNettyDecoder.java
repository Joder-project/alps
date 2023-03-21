package org.alps.core.socket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsPacket;

import java.util.Arrays;

@Slf4j
public class AlpsNettyDecoder extends LengthFieldBasedFrameDecoder {

    private static final int FRAME_MAX_LENGTH =
            Integer.parseInt(System.getProperty("org.alps.frameMaxLength", "134217728"));

    public AlpsNettyDecoder() {
        super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);

            if (null == frame) {
                return null;
            }
            AlpsPacket protocol = read(frame);
            log.debug("receive data: {}, {}", Arrays.toString(protocol.metadata()), Arrays.toString(protocol.data()));
            return protocol;
        } catch (Exception e) {
            log.error("decode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            RemotingHelper.closeChannel(ctx.channel());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }

    AlpsPacket read(ByteBuf byteBuf) {
        var magic = byteBuf.readByte();
        if (magic != AlpsPacket.MAGIC_NUM) {
            return null;
        }
        var module = byteBuf.readShort();
        boolean hasExtHeader = (byteBuf.readByte() & 0x80) > 0;
        byte[] metadata;
        int metadataSize;
        // read header
        byteBuf = byteBuf.readerIndex(byteBuf.readerIndex() - 1);
        if (hasExtHeader) {
            metadataSize = byteBuf.readInt();
            metadataSize = metadataSize & 0x7FF_FFFF;
        } else {
            metadataSize = byteBuf.readShort();
        }
        metadata = new byte[metadataSize];
        byteBuf.readBytes(metadata);
        boolean hasData = (byteBuf.readByte() & 0x80) > 0;
        int dataSize = 0;
        byte[] data = new byte[0];
        if (hasData) {
            byteBuf = byteBuf.readerIndex(byteBuf.readerIndex() - 1);
            // read data
            dataSize = byteBuf.readInt();
            dataSize = dataSize & 0x7FF_FFFF;
            data = new byte[dataSize];
            byteBuf.readBytes(data, 0, dataSize);
        }
        return new AlpsPacket(magic, module, hasExtHeader, metadataSize, metadata, hasData, dataSize, data);
    }
}
