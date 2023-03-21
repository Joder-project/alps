package org.alps.starter;

import org.alps.core.AlpsData;
import org.alps.core.AlpsEnhancedSession;
import org.alps.core.AlpsMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AlpsExchange {

    private final AlpsEnhancedSession session;
    private final AlpsMetadata inMetadata;
    private final AlpsData inData;

    private final Map<String, Object> metadata = new HashMap<>();

    public AlpsExchange(AlpsEnhancedSession session, AlpsMetadata inMetadata, AlpsData inData) {
        this.session = session;
        this.inMetadata = inMetadata;
        this.inData = inData;
    }

    Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * 获取输入流的metadata
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> inMetadataValue(String key, Class<T> clazz) {
        return inMetadata.getValue(key, clazz).map(e -> (T) e);
    }

    /**
     * 添加输出流metadata
     *
     * @param key
     * @param value
     * @return
     */
    public AlpsExchange addMetadataValue(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    public <T> Optional<T> data(Class<T> clazz) {
        return data(0, clazz);
    }

    /**
     * 获取传入的数据
     *
     * @param index
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Optional<T> data(int index, Class<T> clazz) {
        if (index < 0 || index >= inData.dataSize()) {
            return Optional.empty();
        }
        return Optional.ofNullable(inData.dataArray()[index].object(clazz));
    }

    /**
     * 获取session的内存属性
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T attr(String key) {
        return session.attr(key);
    }

    /**
     * 为session设置内存属性
     *
     * @param key
     * @param value
     * @return
     */
    public AlpsExchange attr(String key, Object value) {
        session.attr(key, value);
        return this;
    }

    public SpringAlpsSession session() {
        return new SpringAlpsSession(session);
    }
}
