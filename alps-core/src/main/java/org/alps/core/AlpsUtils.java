package org.alps.core;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.common.AlpsPacketUtils;

import java.util.Collection;

@Slf4j
public class AlpsUtils {

    private AlpsUtils() {

    }

    /**
     * 广播
     *
     * @param sessions 会话
     * @param packet   数据
     */
    public static void broadcast(Collection<? extends AlpsSession> sessions, AlpsPacket packet) {
        if (packet.metadata().frameType() != Frame.FNF) {
            throw new UnsupportedOperationException("不支持非FNF的广播请求");
        }
        try {
            var newPacket = AlpsPacketUtils.encode(packet);
            for (AlpsSession session : sessions) {
                session.send(newPacket);
            }
        } catch (Exception e) {
            log.error("广播数据异常", e);
        }
    }
}
