package org.alps.core;

/**
 * AlpsDataCoder增强器
 * TODO delete?
 * TODO 支持基本类型不走解码器
 */
class AlpsDataCoderDecorator implements AlpsDataCoder {

    private final AlpsDataCoder dataCoder;

    public AlpsDataCoderDecorator(AlpsDataCoder dataCoder) {
        this.dataCoder = dataCoder;
    }

    @Override
    public <T> T encode(byte[] data, int offset, int size, Class<T> clazz) {
        if (isPrimitive(clazz)) {
            return encodePrimitive(data, offset, size, clazz);
        }
        return dataCoder.encode(data, offset, size, clazz);
    }

    @Override
    public byte[] decode(Object obj) {
        if (isPrimitive(obj.getClass())) {
            return decodePrimitive(obj);
        }
        return dataCoder.decode(obj);
    }

    boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || Boolean.class.isAssignableFrom(clazz)
                || Byte.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz) ||
                Short.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz) ||
                Long.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz) ||
                Double.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }

    <T> T encodePrimitive(byte[] data, int offset, int size, Class<T> clazz) {
        // todo
        return dataCoder.encode(data, offset, size, clazz);
    }

    byte[] decodePrimitive(Object obj) {
        // todo
        return dataCoder.decode(obj);
    }
}
