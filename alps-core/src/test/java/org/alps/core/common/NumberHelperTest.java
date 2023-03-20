package org.alps.core.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberHelperTest {

    @Test
    void test_read_int() {
        int value = 1234;
        var bytes = new byte[4];
        NumberHelper.writeInt(value, bytes, 0);
        int result = NumberHelper.readInt(bytes, 0);
        assertEquals(value, result);
    }

    @Test
    void test_read_long() {
        long value = 1234L;
        var bytes = new byte[8];
        NumberHelper.writeLong(value, bytes, 0);
        long result = NumberHelper.readLong(bytes, 0);
        assertEquals(value, result);
    }

}