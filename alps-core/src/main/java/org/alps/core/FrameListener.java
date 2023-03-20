package org.alps.core;

public interface FrameListener {

    /**
     * 监听帧数据
     *
     * @param frame
     */
    void listen(AlpsSession session, Frame frame);
}
