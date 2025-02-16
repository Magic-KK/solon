package org.noear.solon.cloud.tracing.integration;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.utils.LocalUtils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

import java.util.HashSet;
import java.util.Set;

/**
 * Solon 过滤器适配
 *
 * @author noear
 * @since 1.4
 */
public class SolonFilterTracing implements Filter {
    private Tracer tracer;
    private Set<String> excludePaths = new HashSet<>();

    public SolonFilterTracing(String excluded) {
        //跟踪器注入
        Solon.context().getWrapAsyn(Tracer.class, bw -> {
            tracer = bw.raw();
        });

        //排除支持
        if (Utils.isNotEmpty(excluded)) {
            for (String path : excluded.split(",")) {
                path = path.trim();

                if (path.length() > 0) {
                    if (path.startsWith("/")) {
                        excludePaths.add(path);
                    } else {
                        excludePaths.add("/" + path);
                    }
                }
            }
        }
    }

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (tracer == null || excludePaths.contains(ctx.pathNew())) {
            //没有跟踪器，或者排除
            chain.doFilter(ctx);
        } else {
            Span span = buildSpan(ctx);

            try (Scope scope = tracer.activateSpan(span)) {
                chain.doFilter(ctx);
            } catch (Throwable e) {
                span.log(Utils.throwableToString(e));
                throw e;
            } finally {
                span.finish();
            }
        }
    }

    public Span buildSpan(Context ctx) {
        int port = ctx.uri().getPort();
        if (port < 1) {
            port = Solon.cfg().serverPort();
        }
        String ip = LocalUtils.getLocalAddress();

        //实例化构建器
        StringBuilder operationName = new StringBuilder();
        operationName.append(ctx.pathNew()).append(" (").append(ctx.uri().getScheme()).append(" ").append(ip).append(":").append(port).append(')');
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName.toString());

        //添加种类标志
        spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

        //获取上下文
        SpanContext spanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(ctx.headerMap()));
        if (spanContext != null) {
            //如果有，说明是从上传传导过来的
            spanBuilder = spanBuilder.asChildOf(spanContext);
        }

        Span span = spanBuilder.start();

        //尝试注入
        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapAdapter(ctx.headerMap()));

        //开始
        return span;
    }
}
