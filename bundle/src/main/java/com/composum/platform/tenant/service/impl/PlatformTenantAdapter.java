package com.composum.platform.tenant.service.impl;

import com.composum.platform.tenant.service.TenantManagerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlatformTenantAdapter implements AdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTenantAdapter.class);

    protected final TenantManagerService tenantManager;
    protected final List<Pattern> pathPatterns;

    public PlatformTenantAdapter(@Nonnull final TenantManagerService service,
                                 @Nonnull final String[] pathPatternSet) {
        tenantManager = service;
        pathPatterns = new ArrayList<Pattern>() {{
            for (String pattern : pathPatternSet) {
                add(Pattern.compile(pattern));
            }
        }};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType getAdapter(@Nonnull final Object adaptable,
                                                @Nonnull final Class<AdapterType> type) {
        if (adaptable instanceof Resource) {
            Resource resource = (Resource) adaptable;
            if (type == PlatformTenant.class || type == Tenant.class) {
                String path = resource.getPath();
                String tenantId = getTenantId(path);
                if (StringUtils.isNotBlank(tenantId)) {
                    return (AdapterType) tenantManager.getTenant(resource.getResourceResolver(), tenantId);
                }
                return null;
            }
        }
        LOG.warn("can't handle adaptable '{}'", adaptable.getClass().getName());
        return null;
    }

    public String getTenantId(@Nonnull final String path) {
        for (Pattern pattern : pathPatterns) {
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}