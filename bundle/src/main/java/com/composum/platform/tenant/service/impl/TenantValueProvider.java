package com.composum.platform.tenant.service.impl;

import com.composum.pages.commons.service.PlaceholderService;
import com.composum.sling.core.BeanContext;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Tenant Value Provider"
        },
        immediate = true
)
public class TenantValueProvider implements PlaceholderService.ValueProvider {

    @Nonnull
    @Override
    public Integer valueProviderRank() {
        return 10;
    }

    @Nullable
    @Override
    public <T> T getValue(@Nonnull BeanContext context, @Nonnull String key, Class<T> type) {
        if (key.startsWith("tenant.")) {
            Tenant tenant = context.getResource().adaptTo(Tenant.class);
            if (tenant != null) {
                //noinspection unchecked
                return (T) tenant.getProperty(key);
            }
        }
        return null;
    }
}
