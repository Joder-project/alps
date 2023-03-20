package org.alps.core;

/**
 * 携带指令号的帧类型
 */
public interface CommandFrame extends Frame {

    int command();
}
