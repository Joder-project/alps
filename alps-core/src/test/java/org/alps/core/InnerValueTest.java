package org.alps.core;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InnerValueTest {

    @Test
    void data() throws Exception {
        var coder = Mockito.mock(AlpsDataCoder.class);
        byte[] b1 = {10};
        byte[] b2 = {1};
        Mockito.when(coder.decode(10)).thenReturn(b1);
        Mockito.when(coder.decode(true)).thenReturn(b2);
        Mockito.when(coder.encode(b1, int.class)).thenReturn(10);
        Mockito.when(coder.encode(b2, boolean.class)).thenReturn(true);

        var innerValue = new InnerValue(coder, 10);
        assertArrayEquals(b1, innerValue.data());

        var innerValue2 = new InnerValue(coder, 10);
        assertEquals(10, innerValue2.object(int.class));
    }

    @Test
    void testObject() {
    }
}