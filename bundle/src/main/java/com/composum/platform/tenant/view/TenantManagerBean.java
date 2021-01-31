package com.composum.platform.tenant.view;

import com.composum.sling.core.AbstractServletBean;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.filter.StringFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;

public class TenantManagerBean extends AbstractServletBean {

    private transient String viewType;

    public TenantManagerBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public TenantManagerBean(BeanContext context) {
        super(context);
    }

    public TenantManagerBean() {
        super();
    }

    @Nonnull
    public String getPath() {
        return resource.getPath();
    }

    public String getViewType() {
        if (viewType == null) {
            Tenant tenant = resource.adaptTo(Tenant.class);
            viewType = tenant == null ? "root" : "tenant";
        }
        return viewType;
    }

    public String getTabType() {
        String selector = getRequest().getSelectors(new StringFilter.BlackList("^tab$"));
        return StringUtils.isNotBlank(selector) ? selector.substring(1) : "general";
    }
}
