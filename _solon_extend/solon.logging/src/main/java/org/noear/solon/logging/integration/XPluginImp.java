package org.noear.solon.logging.integration;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.logging.AppenderManager;
import org.noear.solon.logging.LogOptions;
import org.noear.solon.logging.event.Appender;
import org.slf4j.MDC;

import java.util.Properties;


/**
 * @author noear
 * @since 1.3
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(AopContext context) {
        loadAppenderConfig();
    }

    private void loadAppenderConfig() {
        Properties props = Solon.cfg().getProp("solon.logging.appender");

        //初始化
        AppenderManager.getInstance();

        //注册添加器
        if (props.size() > 0) {
            props.forEach((k, v) -> {
                String key = (String) k;
                String val = (String) v;

                if (key.endsWith(".class")) {
                    Appender appender = Utils.newInstance(val);
                    if (appender != null) {
                        String name = key.substring(0, key.length() - 6);
                        AppenderManager.getInstance().register(name, appender);
                    }
                }
            });
        }

        //init
        LogOptions.getLoggerLevelInit();

        Solon.app().filter(-9, (ctx, chain) -> {
            MDC.clear();
            chain.doFilter(ctx);
        });
    }

    @Override
    public void stop() throws Throwable {
        AppenderManager.getInstance().stop();
    }
}
