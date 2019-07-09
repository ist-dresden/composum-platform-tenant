package com.composum.platform.tenant.view;

import com.composum.sling.core.AbstractServletBean;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.filter.StringFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;

public class TenantsRootBean extends AbstractServletBean {

    private transient String viewType;

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
}
