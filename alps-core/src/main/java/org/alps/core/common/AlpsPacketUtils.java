package org.alps.core.common;

import com.google.protobuf.*;
import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.alps.core.AlpsPacket;
import org.alps.core.InnerValue;
import org.alps.core.proto.AlpsProtocol;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;

public class AlpsPacketUtils {

    public static AlpsProtocol.AlpsPacket encode(AlpsPacket packet) throws Exception {
        return AlpsProtocol.AlpsPacket.newBuilder()
                .setConnectTypeValue(packet.connectType())
                .setMagic(AlpsPacket.MAGIC_NUM)
                .setModule(packet.module())
                .setMetadata(buildMetadata(packet.metadata()))
                .setData(buildData(packet.data()))
                .build();
    }

    static AlpsProtocol.AlpsPacket.AlpsMetadata buildMetadata(AlpsMetadata metadata) throws Exception {
        var builder = AlpsProtocol.AlpsPacket.AlpsMetadata.newBuilder()
                .setZip(metadata.isZip())
                .setFrameTypeValue(metadata.frameType())
                .setContainerCoderValue(metadata.containerCoder())
                .setFrame(ByteString.copyFrom(metadata.frame()));
        if (metadata.isZip()) {
            builder.setZipContainer(ByteString.copyFrom(GZipHelper.zip(buildContainer(metadata.container()).toByteArray())));
        } else {
            metadata.container().forEach((k, v) -> builder.putContainer(k, ByteString.copyFrom(v.data())));
            builder.setZipContainer(ByteString.EMPTY);
        }
        return builder.build();
    }

    static ByteString buildContainer(Map<String, InnerValue> container) throws Exception {
        if (container == null || container.isEmpty()) {
            return ByteString.EMPTY;
        }
        var descriptor = AlpsProtocol.AlpsPacket.AlpsMetadata.Builder.getDescriptor();
        var entry = MapEntry.newDefaultInstance(descriptor,
                WireFormat.FieldType.STRING, "", WireFormat.FieldType.BYTES, ByteString.EMPTY);
        var mapField = MapField.newMapField(entry);
        container.forEach((k, v) -> mapField.getMutableMap().put(k, ByteString.copyFrom(v.data())));
        try (var os = new ByteArrayOutputStream()) {
            var out = CodedOutputStream.newInstance(os);
            var m = mapField.getMap();
            var keys = new String[m.size()];
            keys = m.keySet().toArray(keys);
            Arrays.sort(keys);
            for (String key : keys) {
                out.writeMessageNoTag(entry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
            }
            return ByteString.copyFrom(os.toByteArray());
        }
    }

    static AlpsProtocol.AlpsPacket.AlpsData buildData(AlpsData data) throws Exception {
        var builder = AlpsProtocol.AlpsPacket.AlpsData.newBuilder()
                .setZip(data.isZip())
                .setDataCoderValue(data.dataCoder());
        if (data.isZip()) {
            builder.setZipDataArray(ByteString.copyFrom(GZipHelper.zip(buildData(data.dataArray()).toByteArray())));
        } else {
            for (int i = 0; i < data.dataArray().length; i++) {
                builder.putDataArray(i, ByteString.copyFrom(data.dataArray()[i].data()));
            }
            builder.setZipDataArray(ByteString.EMPTY);
        }
        return builder.build();
    }

    static ByteString buildData(InnerValue[] data) throws Exception {
        if (data == null || data.length == 0) {
            return ByteString.EMPTY;
        }
        var descriptor = AlpsProtocol.AlpsPacket.AlpsMetadata.Builder.getDescriptor();
        var entry = MapEntry.newDefaultInstance(descriptor,
                WireFormat.FieldType.INT32, 0, WireFormat.FieldType.BYTES, ByteString.EMPTY);
        var mapField = MapField.newMapField(entry);
        for (int i = 0; i < data.length; i++) {
            mapField.getMutableMap().put(i, ByteString.copyFrom(data[i].data()));
        }
        try (var os = new ByteArrayOutputStream()) {
            var out = CodedOutputStream.newInstance(os);
            var m = mapField.getMap();
            var keys = new Integer[m.size()];
            keys = m.keySet().toArray(Integer[]::new);
            Arrays.sort(keys);
            for (var key : keys) {
                out.writeMessageNoTag(entry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
            }
            out.flush();
            os.flush();
            return ByteString.copyFrom(os.toByteArray());
        }
    }
}
