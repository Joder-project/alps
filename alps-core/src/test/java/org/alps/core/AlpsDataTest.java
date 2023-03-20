package org.alps.core;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AlpsDataTest {

    @Test
    void create() {
        var coder = Mockito.mock(AlpsDataCoder.class);
        byte[] b1 = {10};
        byte[] b2 = {1};
        Mockito.when(coder.decode(10)).thenReturn(b1);
        Mockito.when(coder.decode(true)).thenReturn(b2);
        Mockito.when(coder.encode(b1, int.class)).thenReturn(10);
        Mockito.when(coder.encode(b2, boolean.class)).thenReturn(true);
        var dataCoderFactory = Mockito.mock(AlpsDataCoderFactory.class);
        Mockito.when(dataCoderFactory.getCoder((byte) 0)).thenReturn(coder);

        var data = new AlpsDataBuilder()
                .dataCoder((byte) 0)
                .isZip(false)
                .coder(coder)
                .addData(10, true)
                .addData()
                .build();

        var data1 = AlpsData.create(data.toBytes(), dataCoderFactory);
        assertEquals(data.isZip(), data1.isZip());
        assertEquals(data.dataCoder(), data1.dataCoder());
        assertArrayEquals(data.dataArray(), data1.dataArray());
    }

    @Test
    void create_zip() {
        var coder = Mockito.mock(AlpsDataCoder.class);
        byte[] b1 = {10};
        byte[] b2 = {1};
        Mockito.when(coder.decode(10)).thenReturn(b1);
        Mockito.when(coder.decode(true)).thenReturn(b2);
        Mockito.when(coder.encode(b1, int.class)).thenReturn(10);
        Mockito.when(coder.encode(b2, boolean.class)).thenReturn(true);
        var dataCoderFactory = Mockito.mock(AlpsDataCoderFactory.class);
        Mockito.when(dataCoderFactory.getCoder((byte) 0)).thenReturn(coder);

        var data = new AlpsDataBuilder()
                .dataCoder((byte) 0)
                .isZip(true)
                .coder(coder)
                .addData(10, true)
                .addData()
                .build();

        var data1 = AlpsData.create(data.toBytes(), dataCoderFactory);
        assertEquals(data.isZip(), data1.isZip());
        assertEquals(data.dataCoder(), data1.dataCoder());
        assertArrayEquals(data.dataArray(), data1.dataArray());
    }

    @Test
    void create_zip_with_unzip() {
        var coder = Mockito.mock(AlpsDataCoder.class);
        byte[] b1 = new byte[10000];
        Arrays.fill(b1, (byte) 1);
        byte[] b2 = {1};
        Mockito.when(coder.decode(10)).thenReturn(b1);
        Mockito.when(coder.decode(true)).thenReturn(b2);
        Mockito.when(coder.encode(b1, int.class)).thenReturn(10);
        Mockito.when(coder.encode(b2, boolean.class)).thenReturn(true);
        var dataCoderFactory = Mockito.mock(AlpsDataCoderFactory.class);
        Mockito.when(dataCoderFactory.getCoder((byte) 0)).thenReturn(coder);

        var data1 = new AlpsDataBuilder()
                .dataCoder((byte) 0)
                .isZip(true)
                .coder(coder)
                .addData(10, true)
                .addData()
                .build();

        var data2 = new AlpsDataBuilder()
                .dataCoder((byte) 0)
                .isZip(false)
                .coder(coder)
                .addData(10, true)
                .addData()
                .build();

        var bytes = data1.toBytes();
        var bytes1 = data2.toBytes();
        assertTrue(bytes1.length >= bytes.length, "zip error");
    }
}