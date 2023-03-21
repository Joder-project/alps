package org.alps.core.socket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsPacket;

import java.util.Arrays;

@Slf4j
@ChannelHandler.Sharable
public class AlpsNettyEncoder extends MessageToByteEncoder<AlpsPacket> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AlpsPacket protocol, ByteBuf byteBuf) throws Exception {
        try {
            write(protocol, byteBuf);
        } catch (Exception e) {
            log.error("encode exception, " + RemotingHelper.parseChannelRemoteAddr(channelHandlerContext.channel()), e);
            if (protocol != null) {
                log.error(protocol.toString());
            }
            RemotingHelper.closeChannel(channelHandlerContext.channel());
        }
    }

    void write(AlpsPacket protocol, ByteBuf byteBuf) {
        int metadataSize = protocol.metadataSize();
        int byteSize = 3;
        if (protocol.hasExtMetadata()) {
            metadataSize = metadataSize | 0x8000_0000;
            byteSize += 4;
        } else {
            byteSize += 2;
        }
        byteSize += protocol.metadataSize();

        int dataSize = protocol.dataSize();
        if (protocol.hasData()) {
            dataSize = dataSize | 0x8000_0000;
            byteSize += 4;
        } else {
            byteSize += 1;
        }
        byteSize += protocol.dataSize();
        byteBuf.writeInt(byteSize);
        byteBuf.writeByte(protocol.magic());
        byteBuf.writeShort(protocol.module());
        if (protocol.hasExtMetadata()) {
            byteBuf.writeInt(metadataSize);
        } else {
            byteBuf.writeShort(metadataSize);
        }
        byteBuf.writeBytes(protocol.metadata());
        if (protocol.hasData()) {
            byteBuf.writeInt(dataSize);
        } else {
            byteBuf.writeByte(dataSize);
        }
        log.debug("send data: {}, {}", Arrays.toString(protocol.metadata()), Arrays.toString(protocol.data()));
        byteBuf.writeBytes(protocol.data());

    }
}
