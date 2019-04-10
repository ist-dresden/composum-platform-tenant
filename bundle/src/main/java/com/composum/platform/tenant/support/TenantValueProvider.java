package com.composum.platform.tenant.support;

import com.composum.platform.commons.content.service.PlaceholderService;
import com.composum.sling.core.BeanContext;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * the value provider to make tenant properties available als values
 */
@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Tenant Value Provider",
                Constants.SERVICE_RANKING + ":Integer=10"
        },
        immediate = true
)
public class TenantValueProvider implements PlaceholderService.ValueProvider {

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
