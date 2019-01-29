package com.composum.platform.tenant.view;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.sling.core.AbstractServletBean;
import com.composum.sling.core.BeanContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;

public class TenantBean extends AbstractServletBean {

    private transient Tenant tenant;
    private transient TenantManagerService manager;

    public TenantBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public TenantBean(BeanContext context) {
        super(context);
    }

    public TenantBean() {
        super();
    }

    public Tenant getTenant() {
        if (tenant == null) {
            tenant = getManager().getTenant(context.getResolver(), getName());
        }
        return tenant;
    }

    protected TenantManagerService getManager() {
        if (manager == null) {
            manager = context.getService(TenantManagerService.class);
        }
        return manager;
    }
}
