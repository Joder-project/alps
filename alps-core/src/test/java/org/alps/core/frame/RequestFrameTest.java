package org.alps.core.frame;

import org.alps.core.AlpsData;
import org.alps.core.AlpsMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class RequestFrameTest {

    @Test
    void decode_encode() {
        var bytes = new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8
        };
        var metadata = new AlpsMetadata(false, (short) 0, 0L, (byte) 0, bytes, (byte) 0, new HashMap<>(), null);
        var data = AlpsData.EMPTY;

        var coder = new RequestFrame.Coder();
        var frame = coder.decode(metadata, data);
        var bytes1 = frame.toBytes();
        assertArrayEquals(bytes1, bytes, "RequestFrame解析失败");

    }
}