package org.alps.core.support;

import org.alps.core.AlpsEnhancedSession;
import org.alps.core.CommandFrame;
import org.alps.core.Router;

public class ModuleAuthRouter implements Router {
    @Override
    public String module() {
        return null;
    }

    @Override
    public int command() {
        return 0;
    }

    @Override
    public void handle(AlpsEnhancedSession session, CommandFrame frame) throws Throwable {

    }
}
