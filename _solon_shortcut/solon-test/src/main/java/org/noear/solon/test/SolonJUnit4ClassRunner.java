package org.noear.solon.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.aspect.BeanProxy;
import org.noear.solon.core.event.AppInitEndEvent;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.test.annotation.TestPropertySource;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.*;


public class SolonJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    private static final String TAG_classpath = "classpath:";
    private static Set<Class<?>> appCached = new HashSet<>();
    public SolonJUnit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);

        SolonTest anno = klass.getAnnotation(SolonTest.class);
        TestPropertySource propAnno = klass.getAnnotation(TestPropertySource.class);

        if (anno != null) {
            List<String> args = new ArrayList<>();
            if (anno.args().length > 0) {
                args.addAll(Arrays.asList(anno.args()));
            }

            if (anno.debug()) {
                args.add("-debug=1");
            }

            String[] argsStr = args.toArray(new String[args.size()]);

            if (appCached.contains(anno.getClass())) {
                return;
            } else {
                appCached.add(anno.getClass());
            }

            try {
                Method main = getMain(anno);

                if (main != null && Modifier.isStatic(main.getModifiers())) {
                    EventBus.subscribe(AppInitEndEvent.class, e->{
                        //加载测试配置
                        addPropertySource(propAnno);
                    });
                    main.invoke(null, new Object[]{argsStr});
                } else {
                    Solon.start(anno.value(), argsStr, app -> {
                        //加载测试配置
                        addPropertySource(propAnno);
                    });
                }
            } catch (Throwable ex) {
                Utils.throwableUnwrap(ex).printStackTrace();
            }


            //延迟秒数
            if (anno.delay() > 0) {
                try {
                    Thread.sleep(anno.delay() * 1000);
                } catch (Exception ex) {

                }
            }
        } else {
            Solon.start(klass, new String[]{"-debug=1"}, app -> {
                //加载测试配置
                addPropertySource(propAnno);
            });
        }
    }

    private void addPropertySource(TestPropertySource propertySource) {
        if (propertySource == null) {
            return;
        }

        for (String uri : propertySource.value()) {
            if (uri.startsWith(TAG_classpath)) {
                Solon.cfg().loadAdd(uri.substring(TAG_classpath.length()));
            } else {
                try {
                    Solon.cfg().loadAdd(new File(uri).toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Method getMain(SolonTest anno) {
        try {
            return anno.value().getMethod("main", String[].class);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected Object createTest() throws Exception {
        Object tmp = super.createTest();
        Solon.context().beanInject(tmp);

        tmp = BeanProxy.getGlobal().getProxy(Solon.context(),tmp);

        return tmp;
    }
}
