package org.noear.solon.cloud.impl;

import org.noear.solon.Solon;
import org.noear.solon.cloud.CloudJobHandler;
import org.noear.solon.cloud.CloudJobInterceptor;
import org.noear.solon.cloud.service.CloudJobService;

/**
 * @author noear
 * @since 1.6
 */
public class CloudJobServiceManagerImpl implements CloudJobServiceManager {
    private final CloudJobService service;
    private CloudJobInterceptor jobInterceptor;

    public CloudJobServiceManagerImpl(CloudJobService service) {
        this.service = service;
        Solon.context().getWrapAsyn(CloudJobInterceptor.class, bw -> {
            jobInterceptor = bw.get();
        });
    }

    @Override
    public CloudJobInterceptor getJobInterceptor() {
        return jobInterceptor;
    }

    @Override
    public boolean register(String name, String cron7x, String description, CloudJobHandler handler) {
        return service.register(name, cron7x, description, handler);
    }

    @Override
    public boolean isRegistered(String name) {
        return service.isRegistered(name);
    }
}
