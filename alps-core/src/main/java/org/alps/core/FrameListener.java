package org.alps.core;

public interface FrameListener {

    /**
     * 监听帧数据
     */
    void listen(AlpsSession session, Frame frame);
}
