package org.alps.core.socket.netty;

import com.google.protobuf.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.*;
import org.alps.core.common.GZipHelper;
import org.alps.core.proto.AlpsProtocol;

import java.util.*;

@Slf4j
@ChannelHandler.Sharable
public class AlpsMessageDecoder extends MessageToMessageDecoder<AlpsProtocol.AlpsPacket> {

    private final AlpsDataCoderFactory dataCoderFactory;

    public AlpsMessageDecoder(AlpsDataCoderFactory dataCoderFactory) {
        this.dataCoderFactory = dataCoderFactory;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, AlpsProtocol.AlpsPacket alpsPacket, List<Object> list) throws Exception {
        var packet = new AlpsPacket(alpsPacket.getConnectTypeValue(), alpsPacket.getModule(),
                buildMetadata(alpsPacket.getMetadata()), buildData(alpsPacket.getData()), alpsPacket);
        list.add(packet);
    }

    AlpsMetadata buildMetadata(AlpsProtocol.AlpsPacket.AlpsMetadata metadata) throws Exception {
        boolean isZip = metadata.getZip();
        byte frameType = (byte) metadata.getFrameTypeValue();
        byte[] frame = metadata.getFrame().toByteArray();
        byte containerCoder = (byte) metadata.getContainerCoderValue();
        var coder = dataCoderFactory.getCoder(containerCoder);
        Map<String, InnerValue> container;
        if (isZip) {
            container = parseContainer(GZipHelper.unzip(metadata.getZipContainer().toByteArray()), coder);
        } else {
            var map = new HashMap<String, InnerValue>();
            metadata.getContainerMap().forEach((k, v) -> map.put(k, new InnerValue(coder, v.toByteArray())));
            container = Collections.unmodifiableMap(map);
        }

        return new AlpsMetadata(isZip, frameType, frame, containerCoder, container, coder);
    }

    AlpsData buildData(AlpsProtocol.AlpsPacket.AlpsData data) throws Exception {
        if (data == null) {
            return AlpsData.EMPTY;
        }
        boolean isZip = data.getZip();
        byte dataCoder = (byte) data.getDataCoderValue();
        var coder = dataCoderFactory.getCoder(dataCoder);
        InnerValue[] dataArray;
        if (isZip) {
            dataArray = parseData(GZipHelper.unzip(data.getZipDataArray().toByteArray()), coder);
        } else {
            dataArray = new TreeMap<>(data.getDataArrayMap())
                    .values()
                    .stream()
                    .map(e -> new InnerValue(coder, e.toByteArray()))
                    .toArray(InnerValue[]::new);
        }
        return new AlpsData(isZip, dataCoder, coder, dataArray);
    }


    Map<String, InnerValue> parseContainer(byte[] data, AlpsDataCoder coder) throws Exception {
        var descriptor = AlpsProtocol.AlpsPacket.AlpsMetadata.Builder.getDescriptor().getNestedTypes().get(0);
        var entry = MapEntry.newDefaultInstance(descriptor,
                WireFormat.FieldType.STRING, "", WireFormat.FieldType.BYTES, ByteString.EMPTY);
        var inputStream = CodedInputStream.newInstance(data);
        var map = new HashMap<String, InnerValue>();
        while (inputStream.getBytesUntilLimit() > 0) {
            var resultArray = inputStream.readMessage(entry.getParserForType(), ExtensionRegistryLite.getEmptyRegistry());
            map.put(resultArray.getKey(), parseBytes(coder, resultArray.getValue().toByteArray()));
        }
        return Collections.unmodifiableMap(map);
    }

    InnerValue[] parseData(byte[] data, AlpsDataCoder coder) throws Exception {
        var descriptor = AlpsProtocol.AlpsPacket.AlpsMetadata.Builder.getDescriptor().getNestedTypes().get(0);
        var entry = MapEntry.newDefaultInstance(descriptor,
                WireFormat.FieldType.INT32, 0, WireFormat.FieldType.BYTES, ByteString.EMPTY);
        var inputStream = CodedInputStream.newInstance(data);
        var map = new TreeMap<Integer, InnerValue>();
        while (inputStream.getBytesUntilLimit() > 0) {
            var resultArray = inputStream.readMessage(entry.getParserForType(), ExtensionRegistryLite.getEmptyRegistry());
            map.put(resultArray.getKey(), parseBytes(coder, resultArray.getValue().toByteArray()));
        }
        return map.values().toArray(new InnerValue[0]);
    }

    InnerValue parseBytes(AlpsDataCoder coder, byte[] data) {
        if (data == null || data.length == 0) {
            return new InnerValue(coder, new byte[0]);
        } else {
            return new InnerValue(coder, data);
        }
    }
}
