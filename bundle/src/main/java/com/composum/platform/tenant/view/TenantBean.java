package com.composum.platform.tenant.view;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.sling.core.AbstractServletBean;
import com.composum.sling.core.BeanContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collection;

public class TenantBean extends AbstractServletBean {

    private static final Logger LOG = LoggerFactory.getLogger(TenantBean.class);

    private transient Tenant tenant;

    private transient Collection<TenantUserManager.TenantUser> users;

    private transient TenantManagerService manager;
    private transient TenantUserManager userManager;

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

    // users

    public Collection<TenantUserManager.TenantUser> getUsers() {
        if (users == null) {
            try {
                users = getUserManager().getTenantUsers(context.getResolver(), getTenant().getId());
            } catch (RepositoryException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return users;
    }

    protected TenantUserManager getUserManager() {
        if (userManager == null) {
            userManager = context.getService(TenantUserManager.class);
        }
        return userManager;
    }
}
