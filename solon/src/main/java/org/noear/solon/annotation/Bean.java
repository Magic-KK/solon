package org.noear.solon.annotation;

import java.lang.annotation.*;

/**
 * 通用 组件（只能配合 @Configuration 使用）
 *
 * <pre><code>
 * //注解在配置器的函数上
 * @Configuration
 * public class Config{
 *     @Bean
 *     public DataSource db1(@Inject("${db1}") HikariDataSource ds){
 *         return ds;
 *     }
 * }
 * </code></pre>
 *
 * @author noear
 * @since 1.0
 * */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    @Alias("name")
    String value() default ""; //as bean.name

    @Alias("value")
    String name() default "";

    @Note("标签，用于快速查找")
    String tag() default "";

    @Deprecated
    @Note("特性，用于辅助配置")
    String[] attrs() default {};

    @Note("同时注册类型，仅当名称非空时有效")
    boolean typed() default false;

    @Note("顺序位，仅某些类型有效")
    int index() default 0;
}
