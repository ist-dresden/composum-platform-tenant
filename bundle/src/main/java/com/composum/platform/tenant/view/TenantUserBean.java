package com.composum.platform.tenant.view;

import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.composum.platform.tenant.servlet.AbstractTenantServlet.PARAM_USER_ID;

public class TenantUserBean extends AbstractTenantBean {

    private static final Logger LOG = LoggerFactory.getLogger(TenantUserBean.class);

    private transient TenantUserManager.TenantUser user;

    public TenantUserBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public TenantUserBean(BeanContext context) {
        super(context);
    }

    public TenantUserBean() {
        super();
    }

    public boolean isValid() {
        return getTenant() != null && getUser() != null;
    }

    public TenantUserManager.TenantUser getUser() {
        if (user == null) {
            Tenant tenant = getTenant();
            String userId = RequestUtil.getParameter(getRequest(), PARAM_USER_ID, "");
            if (StringUtils.isBlank(userId)) {
                userId = getResolver().getUserID();
            }
            if (tenant != null && StringUtils.isNotBlank(userId)) {
                try {
                    user = context.getService(TenantUserManager.class)
                            .getTenantUser(getResolver(), tenant.getId(), userId);
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        return user;
    }
}
