package org.noear.solon.extend.mybatis.integration;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.extend.mybatis.MybatisAdapter;
import org.noear.solon.extend.mybatis.MybatisAdapterFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 适配管理器
 *
 * @author noear
 * @since 1.1
 * */
public class MybatisAdapterManager {
    private static MybatisAdapterFactory adapterFactory = new MybatisAdapterFactoryDefault();

    /**
     * 设置适配器工厂
     */
    public static void setAdapterFactory(MybatisAdapterFactory adapterFactory) {
        MybatisAdapterManager.adapterFactory = adapterFactory;
    }

    /**
     * 缓存适配器
     */
    private static Map<String, MybatisAdapter> dbMap = new ConcurrentHashMap<>();

    /**
     * 获取适配器
     */
    public static MybatisAdapter get(BeanWrap bw) {
        MybatisAdapter db = dbMap.get(bw.name());

        if (db == null) {
            synchronized (bw.name().intern()) {
                db = dbMap.get(bw.name());
                if (db == null) {
                    db = buildAdapter(bw);

                    dbMap.put(bw.name(), db);

                    if (bw.typed()) {
                        dbMap.put("", db);
                    }
                }
            }

        }

        return db;
    }

    /**
     * 注册数据源，并生成适配器
     *
     * @param bw 数据源的BW
     */
    public static void register(BeanWrap bw) {
        get(bw);
    }

    /**
     * 构建适配器
     */
    private static MybatisAdapter buildAdapter(BeanWrap bw) {
        MybatisAdapter adapter;

        if (Utils.isEmpty(bw.name())) {
            adapter = adapterFactory.create(bw);
        } else {
            adapter = adapterFactory.create(bw, Solon.cfg().getProp("mybatis." + bw.name()));
        }

        //就扫一次
        MybatisMapperScanner.mapperScan(bw, adapter);

        return adapter;
    }
}
