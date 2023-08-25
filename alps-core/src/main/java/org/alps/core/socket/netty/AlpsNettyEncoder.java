package org.alps.core.socket.netty;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

@Slf4j
@ChannelHandler.Sharable
public class AlpsNettyEncoder extends ProtobufEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
        if (msg instanceof MessageLite messageLite) {
            var size = Unpooled.copyInt(messageLite.getSerializedSize());
            var content = wrappedBuffer(messageLite.toByteArray());
            out.add(size);
            out.add(content);
            return;
        }
        if (msg instanceof MessageLite.Builder builder) {
            var build = builder.build();
            var size = Unpooled.copyInt(build.getSerializedSize());
            var content = wrappedBuffer(build.toByteArray());
            out.add(size);
            out.add(content);
        }
    }
}
