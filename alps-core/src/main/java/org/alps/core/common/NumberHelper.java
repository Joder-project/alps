package org.alps.core.common;

public class NumberHelper {

    public static short readShort(byte[] data, int offset) {
        return (short) (((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF));
    }

    public static void writeShort(short value, byte[] out, int offset) {
        out[offset] = (byte) ((value >> 8) & 0xFF);
        out[offset + 1] = (byte) (value & 0xFF);
    }

    public static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
                ((data[offset + 1] & 0xFF) << 16) |
                ((data[offset + 2] & 0xFF) << 8) |
                (data[offset + 3] & 0xFF);
    }

    public static void writeInt(int value, byte[] out, int offset) {
        out[offset] = (byte) ((value >> 24) & 0xFF);
        out[offset + 1] = (byte) ((value >> 16) & 0xFF);
        out[offset + 2] = (byte) ((value >> 8) & 0xFF);
        out[offset + 3] = (byte) (value & 0xFF);
    }

    public static long readLong(byte[] data, int offset) {
        return ((long) (data[offset] & 0xFF) << 56) |
                ((long) (data[offset + 1] & 0xFF) << 48) |
                ((long) (data[offset + 2] & 0xFF) << 40) |
                ((long) (data[offset + 3] & 0xFF) << 32) |
                ((long) (data[offset + 4] & 0xFF) << 24) |
                ((data[offset + 5] & 0xFF) << 16) |
                ((data[offset + 6] & 0xFF) << 8) |
                (data[offset + 7] & 0xFF);
    }

    public static void writeLong(long value, byte[] out, int offset) {
        out[offset] = (byte) ((value >> 56) & 0xFF);
        out[offset + 1] = (byte) ((value >> 48) & 0xFF);
        out[offset + 2] = (byte) ((value >> 40) & 0xFF);
        out[offset + 3] = (byte) ((value >> 32) & 0xFF);
        out[offset + 4] = (byte) ((value >> 24) & 0xFF);
        out[offset + 5] = (byte) ((value >> 16) & 0xFF);
        out[offset + 6] = (byte) ((value >> 8) & 0xFF);
        out[offset + 7] = (byte) (value & 0xFF);
    }
}
