package org.alps.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsEnhancedSession;
import org.alps.core.AlpsSession;
import org.alps.core.Frame;
import org.alps.core.FrameListener;
import org.alps.core.common.AlpsAuthException;
import org.alps.core.frame.ModuleAuthFrame;
import org.alps.core.proto.Errors;

/**
 * 模块认证
 */
@Slf4j
public class ModuleAuthFrameListener implements FrameListener {

    @Override
    public void listen(AlpsSession session, Frame frame) {
        if (session.isAuth()) {
            return;
        }
        var moduleAuthFrame = (ModuleAuthFrame) frame;
        try {
            session.auth(moduleAuthFrame.version(), moduleAuthFrame.verifyToken());
        } catch (AlpsAuthException ex) {
            var enhancedSession = (AlpsEnhancedSession) session;
            enhancedSession.error().code(Errors.Code.Module_Auth_Error_VALUE).send().subscribe();
        }
    }
}
