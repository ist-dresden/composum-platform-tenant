package com.composum.platform.tenant.view;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.impl.PlatformTenant;
import com.composum.platform.tenant.util.TenantComparator;
import com.composum.sling.core.AbstractServletBean;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.filter.StringFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TenantsRootBean extends AbstractServletBean {

    private transient String viewType;

    private transient List<PlatformTenant> tenants;

    public TenantsRootBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public TenantsRootBean(BeanContext context) {
        super(context);
    }

    public TenantsRootBean() {
        super();
    }

    @Nonnull
    public String getPath() {
        return resource.getPath();
    }

    public String getViewType() {
        return "root";
    }

    public String getTabType() {
        String selector = getRequest().getSelectors(new StringFilter.BlackList("^tab$"));
        return StringUtils.isNotBlank(selector) ? selector.substring(1) : "general";
    }

    public Collection<PlatformTenant> getTenants() {
        if (tenants == null) {
            tenants = new ArrayList<>();
            ResourceResolver resolver = context.getResolver();
            TenantManagerService tenantManager = context.getService(TenantManagerService.class);
            Iterator<Tenant> iterator = tenantManager.getTenants(resolver, null);
            while (iterator.hasNext()) {
                tenants.add((PlatformTenant) iterator.next());
            }
            tenants.sort(TenantComparator.INSTANCE);
        }
        return tenants;
    }
}
