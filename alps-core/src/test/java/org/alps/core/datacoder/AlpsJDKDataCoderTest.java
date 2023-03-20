package org.alps.core.datacoder;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlpsJDKDataCoderTest {

    record A(int f1, char f2, long f3) implements Serializable {
    }

    @Test
    void encode_decode() {
        var coder = new AlpsJDKDataCoder();
        int a = 1;
        assertEquals(a, coder.encode(coder.decode(a), int.class));
        assertEquals(a, coder.encode(coder.decode(a), Integer.class));

        boolean b = true;
        assertEquals(b, coder.encode(coder.decode(b), boolean.class));
        assertEquals(b, coder.encode(coder.decode(b), Boolean.class));

        String c = "true";
        assertEquals(c, coder.encode(coder.decode(c), String.class));

        A d = new A(1, 'a', 2L);
        assertEquals(d, coder.encode(coder.decode(d), A.class));
    }
}