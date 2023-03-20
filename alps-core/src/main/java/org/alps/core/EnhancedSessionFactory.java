package org.alps.core;

/**
 * 增强session创建器
 */
public interface EnhancedSessionFactory {

    AlpsEnhancedSession create(AlpsSession session);
}
