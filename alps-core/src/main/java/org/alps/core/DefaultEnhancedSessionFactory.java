package org.alps.core;

/**
 * 默认增强session创建器
 */
public class DefaultEnhancedSessionFactory implements EnhancedSessionFactory {

    final FrameCoders frameCoders;
    final AlpsDataCoderFactory dataCoderFactory;
    final FrameListeners frameListeners;
    final AlpsConfig config;

    public DefaultEnhancedSessionFactory(FrameCoders frameCoders, AlpsDataCoderFactory dataCoderFactory,
                                         FrameListeners frameListeners, AlpsConfig config) {
        this.frameCoders = frameCoders;
        this.dataCoderFactory = dataCoderFactory;
        this.frameListeners = frameListeners;
        this.config = config;
    }

    @Override
    public AlpsEnhancedSession create(AlpsSession session) {
        return new AlpsEnhancedSession(session, frameCoders, dataCoderFactory, frameListeners, config);
    }
}
