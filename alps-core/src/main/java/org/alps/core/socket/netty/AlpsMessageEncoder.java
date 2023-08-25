package org.alps.core.socket.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsPacket;
import org.alps.core.common.AlpsPacketUtils;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class AlpsMessageEncoder extends MessageToMessageEncoder<AlpsPacket> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AlpsPacket alpsPacket, List<Object> list) throws Exception {
        list.add(AlpsPacketUtils.encode(alpsPacket));
    }


}
