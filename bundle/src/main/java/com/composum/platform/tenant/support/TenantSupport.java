package com.composum.platform.tenant.support;

import com.composum.pages.commons.service.PagesTenantSupport;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.impl.PlatformTenant;
import com.composum.sling.core.BeanContext;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Pages Tenant Support"
        },
        immediate = true
)
public class TenantSupport implements PagesTenantSupport {

    @Reference
    private TenantManagerService tenantManager;

    @Nonnull
    @Override
    public Collection<String> getTenantIds(@Nonnull final BeanContext context) {
        List<String> result = new ArrayList<>();
        Iterator<Tenant> iterator = tenantManager.getTenants(context.getResolver(), null);
        while (iterator.hasNext()) {
            result.add(iterator.next().getId());
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
}
