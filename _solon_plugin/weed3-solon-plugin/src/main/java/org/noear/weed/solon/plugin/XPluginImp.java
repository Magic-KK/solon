package org.noear.weed.solon.plugin;

import org.noear.solon.Solon;
import org.noear.solon.core.*;
import org.noear.weed.WeedConfig;
import org.noear.weed.annotation.Db;
import org.noear.weed.xml.XmlSqlLoader;

/**
 * @author noear
 * @since 1.3
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(AopContext context) {
        // 事件监听，用于时实初始化
        Solon.app().onEvent(BeanWrap.class, new DsEventListener());

        // 切换Weed的链接工厂，交于Solon托管这
        WeedConfig.connectionFactory = new DsConnectionFactoryImpl();

        // 添加响应器（构建、注入）
        DbBeanReactor beanReactor = new DbBeanReactor();

        context.beanBuilderAdd(Db.class, beanReactor);
        context.beanInjectorAdd(Db.class, beanReactor);

        // 加载xml sql
        XmlSqlLoader.tryLoad();
    }
}
