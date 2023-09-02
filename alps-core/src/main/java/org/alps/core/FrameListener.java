package org.alps.core;

import org.alps.core.proto.AlpsProtocol;

public interface FrameListener {

    /**
     * 监听帧数据
     *
     * @param frame
     */
    void listen(AlpsSession session, Frame frame);
}
