package org.alps.core;

/**
 * 默认增强session创建器
 */
public class DefaultEnhancedSessionFactory implements EnhancedSessionFactory {

    final FrameCoders frameCoders;
    final AlpsDataCoderFactory dataCoderFactory;
    final FrameListeners frameListeners;
    final SessionListeners sessionListeners;
    final AlpsConfig config;

    public DefaultEnhancedSessionFactory(FrameCoders frameCoders, AlpsDataCoderFactory dataCoderFactory,
                                         FrameListeners frameListeners, SessionListeners sessionListeners, AlpsConfig config) {
        this.frameCoders = frameCoders;
        this.dataCoderFactory = dataCoderFactory;
        this.frameListeners = frameListeners;
        this.config = config;
        this.sessionListeners = sessionListeners;
    }

    @Override
    public AlpsEnhancedSession create(AlpsSession session) {
        return new AlpsEnhancedSession(session, frameCoders, dataCoderFactory, frameListeners, sessionListeners, config);
    }
}
