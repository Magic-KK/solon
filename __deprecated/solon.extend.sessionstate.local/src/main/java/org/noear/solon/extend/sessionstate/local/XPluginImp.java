package org.noear.solon.extend.sessionstate.local;

import org.noear.solon.Solon;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.Bridge;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.PrintUtil;

@Deprecated
public class XPluginImp implements Plugin {
    @Override
    public void start(AopContext context) {
        if (Solon.app().enableSessionState() == false) {
            return;
        }

        if (Bridge.sessionStateFactory().priority() >= LocalSessionStateFactory.SESSION_STATE_PRIORITY) {
            return;
        }

        SessionProp.init();

        Bridge.sessionStateFactorySet(LocalSessionStateFactory.getInstance());

        PrintUtil.info("Session","solon: Local session state plugin is loaded");
    }
}
