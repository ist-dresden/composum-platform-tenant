package com.composum.platform.tenant.view;

import com.composum.platform.tenant.service.HostManagerService;
import com.composum.platform.tenant.service.HostManagerService.Host;
import com.composum.platform.tenant.service.HostManagerService.HostList;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.tenant.service.TenantUserManager.TenantUsers;
import com.composum.sling.core.BeanContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import static com.composum.platform.tenant.servlet.HostManagerServlet.PARAM_HOSTNAME;

public class TenantBean extends AbstractTenantBean {

    private static final Logger LOG = LoggerFactory.getLogger(TenantBean.class);

    public static final String ATTR_HOST = "host";
    public static final String ATTR_HOSTNAME = "hostname";

    private transient TenantUsers users;

    private transient HostList hosts;
    private transient Host host;

    private transient TenantManagerService manager;
    private transient TenantUserManager userManager;
    private transient HostManagerService hostManager;

    public TenantBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public TenantBean(BeanContext context) {
        super(context);
    }

    public TenantBean() {
        super();
    }

    protected TenantManagerService getManager() {
        if (manager == null) {
            manager = context.getService(TenantManagerService.class);
        }
        return manager;
    }

    public String getHintSelector() {
        String[] selectors = request.getRequestPathInfo().getSelectors();
        return selectors.length > 1 ? selectors[1] : "";
    }

    // users

    public int getCountUsers() {
        return getUsers().size();
    }

    public TenantUsers getUsers() {
        if (users == null) {
            try {
                users = getUserManager().getTenantUsers(context.getResolver(), getTenant().getId());
            } catch (RepositoryException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return users;
    }

    protected TenantUserManager getUserManager() {
        if (userManager == null) {
            userManager = context.getService(TenantUserManager.class);
        }
        return userManager;
    }

    // hosts

    public String getPublicHostname() {
        return getHostManager().getPublicHostname();
    }

    public Host getHost() {
        if (host == null) {
            SlingHttpServletRequest request = getRequest();
            host = (Host) request.getAttribute(ATTR_HOST);
            if (host == null) {
                String hostname = (String) request.getAttribute(ATTR_HOSTNAME);
                if (StringUtils.isBlank(hostname)) {
                    hostname = request.getParameter(PARAM_HOSTNAME);
                }
                if (StringUtils.isNotBlank(hostname)) {
                    host = getHosts().get(hostname);
                }
            }
        }
        return host;
    }

    public HostList getHosts() {
        if (hosts == null) {
            try {
                Tenant tenant = getTenant();
                hosts = getHostManager().hostList(getResolver(), tenant != null ? tenant.getId() : null);
            } catch (HostManagerService.ProcessException ex) {
                LOG.error(ex.getMessage(), ex);
                hosts = new HostList();
            }
        }
        return hosts;
    }

    protected HostManagerService getHostManager() {
        if (hostManager == null) {
            hostManager = context.getService(HostManagerService.class);
        }
        return hostManager;
    }
}
