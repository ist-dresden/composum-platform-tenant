package com.composum.platform.tenant.view;

import com.composum.sling.core.AbstractServletBean;
import com.composum.sling.core.BeanContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTenantBean extends AbstractServletBean {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTenantBean.class);

    private transient Tenant tenant;

    public AbstractTenantBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public AbstractTenantBean(BeanContext context) {
        super(context);
    }

    public AbstractTenantBean() {
        super();
    }

    public Tenant getTenant() {
        if (tenant == null) {
            RequestPathInfo pathInfo = getRequest().getRequestPathInfo();
            String suffix = pathInfo.getSuffix();
            if (StringUtils.isNotBlank(suffix)) {
                Resource resource = getResolver().getResource(suffix);
                if (resource != null) {
                    tenant = resource.adaptTo(Tenant.class);
                }
            }
            if (tenant == null) {
                tenant = getResource().adaptTo(Tenant.class);
            }
        }
        return tenant;
    }
}
