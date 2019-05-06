package com.composum.platform.tenant.support;

import com.composum.pages.commons.service.PagesTenantSupport;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.tenant.service.impl.PlatformTenant;
import com.composum.sling.core.BeanContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Pages Tenant Support"
        },
        immediate = true
)
public class TenantSupport implements PagesTenantSupport {

    private static final Logger LOG = LoggerFactory.getLogger(TenantSupport.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private TenantManagerService tenantManager;

    @Reference
    private TenantUserManager userManager;

    /**
     * @return the tenant of the resource
     */
    @Nullable
    public String getTenantId(@Nonnull Resource resource) {
        Tenant tenant = resource.adaptTo(Tenant.class);
        return tenant != null ? tenant.getId() : null;
    }

    /**
     * @return the list of id/tenant pairs of the joined tenants in the context of the current request
     */
    @Nonnull
    @Override
    public Map<String, Tenant> getTenants(@Nonnull final BeanContext context) {
        Map<String, Tenant> result = new HashMap<>();
        Iterator<Tenant> iterator = tenantManager.getTenants(context.getResolver(), null);
        while (iterator.hasNext()) {
            Tenant tenant = iterator.next();
            result.put(tenant.getId(), tenant);
        }
        return result;
    }

    @Nullable
    @Override
    public String getContentRoot(@Nonnull final BeanContext context, @Nonnull final String tenantId) {
        PlatformTenant tenant = (PlatformTenant) tenantManager.getTenant(context.getResolver(), tenantId);
        return tenant != null ? tenant.getContentRoot() : null;
    }

    @Nullable
    @Override
    public String getApplicationRoot(@Nonnull final BeanContext context, @Nonnull final String tenantId) {
        PlatformTenant tenant = (PlatformTenant) tenantManager.getTenant(context.getResolver(), tenantId);
        return tenant != null ? tenant.getApplicationRoot() : null;
    }

    /**
     * @return 'true' if the contexts user has the 'developer' role
     */
    @Override
    public boolean isDevelopModeAllowed(@Nonnull BeanContext context, @Nullable Resource focus) {
        TenantUserManager.TenantUser user = null;
        ResourceResolver resolver = context.getResolver();
        Tenant tenant = (focus != null ? focus : context.getResource()).adaptTo(Tenant.class);
        if (tenant != null) {
            String userId = resolver.getUserID();
            if (StringUtils.isNotBlank(userId)) {
                switch (userId) {
                    case "anonymous":
                        return false;
                    case "admin":
                        return true;
                    default:
                        return userManager.isInRole(tenant.getId(), TenantUserManager.Role.developer, userId);
                }
            }
        }
        return false;
    }
}
