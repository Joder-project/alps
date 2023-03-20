package org.alps.core;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AlpsMetadataTest {

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

        var metadata = new AlpsMetadataBuilder().isZip(false)
                .verifyToken(1234L)
                .version(((short) 1))
                .containerCoder(((byte) 0))
                .coder(coder)
                .frame(new byte[]{1, 2, 3, 4})
                .frameType(((byte) 3))
                .addMetadata("hello", 10)
                .addMetadata("hello3", true)
                .build();

        var metadata1 = AlpsMetadata.create(metadata.toBytes(), dataCoderFactory);
        assertEquals(metadata.isZip(), metadata1.isZip());
        assertEquals(metadata.frameType(), metadata1.frameType());
        assertEquals(metadata.verifyToken(), metadata1.verifyToken());
        assertEquals(metadata.containerCoder(), metadata1.containerCoder());
        assertEquals(metadata.version(), metadata1.version());
        assertIterableEquals(metadata.container().keySet(), metadata1.container().keySet());
        assertArrayEquals(metadata.frame(), metadata1.frame());

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

        var metadata = new AlpsMetadataBuilder().isZip(true)
                .verifyToken(1234L)
                .version(((short) 1))
                .containerCoder(((byte) 0))
                .coder(coder)
                .frame(new byte[]{1, 2, 3, 4})
                .frameType(((byte) 3))
                .addMetadata("hello", 10)
                .addMetadata("hello3", true)
                .build();

        var metadata1 = AlpsMetadata.create(metadata.toBytes(), dataCoderFactory);
        assertEquals(metadata.isZip(), metadata1.isZip());
        assertEquals(metadata.frameType(), metadata1.frameType());
        assertEquals(metadata.verifyToken(), metadata1.verifyToken());
        assertEquals(metadata.containerCoder(), metadata1.containerCoder());
        assertEquals(metadata.version(), metadata1.version());
        assertIterableEquals(metadata.container().keySet(), metadata1.container().keySet());
        assertArrayEquals(metadata.frame(), metadata1.frame());

    }
}