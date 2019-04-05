package com.composum.platform.tenant.view;

import com.composum.platform.models.simple.SimpleModel;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.workflow.service.WorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

public class HomePageBean extends SimpleModel {

    private static final Logger LOG = LoggerFactory.getLogger(HomePageBean.class);

    public static final String PAGES_HOME_PATH = "/libs/composum/pages/stage/home";

    public static final String TENANT_CONSOLE_PATH = "/libs/composum/platform/tenant";
    public static final String TENANT_CONSOLE_URI = "/bin/platform/tenants.html";

    private transient Boolean openWorkflows;
    private transient Boolean tenantsAvailable;
    private transient Tenant firstTenant;

    public String getUserId() {
        return getResolver().getUserID();
    }

    public boolean forwardToSites() {
        if (isTenantsAvailable()) {
            Resource pagesHome = context.getResolver().getResource(PAGES_HOME_PATH);
            if (pagesHome != null) {
                SlingHttpServletRequest request = context.getRequest();
                RequestDispatcher dispatcher = request.getRequestDispatcher(pagesHome);
                if (dispatcher != null) {
                    try {
                        dispatcher.forward(request, context.getResponse());
                        return true;
                    } catch (ServletException | IOException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                } else {
                    LOG.error("can't forward to '{}'", PAGES_HOME_PATH);
                }
            } else {
                TenantManagerService service = context.getService(TenantManagerService.class);
                Tenant tenant = getFirstTenant();
                Resource tenantsConsole = context.getResolver().getResource(TENANT_CONSOLE_PATH);
                if (service != null && tenantsConsole != null && tenant != null) {
                    SlingHttpServletRequest request = context.getRequest();
                    SlingHttpServletResponse response = context.getResponse();
                    try {
                        response.sendRedirect(request.getContextPath() + TENANT_CONSOLE_URI
                                + service.getTenantsRoot(context.getResolver()).getPath() + "/" + tenant.getId());
                        return true;
                    } catch (IOException ex) {
                        LOG.error("can't redirect to '{}' ({})", TENANT_CONSOLE_URI, ex.toString());
                    }
                } else {
                    LOG.error("Pages home and Tenant console not available.");
                }
            }
        }
        return false;
    }

    public boolean isOpenWorkflows() {
        if (openWorkflows == null) {
            String userId = context.getResolver().getUserID();
            WorkflowService service = context.getService(WorkflowService.class);
            openWorkflows = service != null && StringUtils.isNotBlank(userId)
                    && !service.findInitiatedOpenWorkflows(context, userId).isEmpty();
            if (LOG.isDebugEnabled()) {
                LOG.debug("openWorkflows({}): {}", userId, openWorkflows);
            }
        }
        return openWorkflows;
    }

    public boolean isTenantsAvailable() {
        if (tenantsAvailable == null) {
            tenantsAvailable = getFirstTenant() != null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("tenantsAvailable({}): {}", context.getResolver().getUserID(), tenantsAvailable);
            }
        }
        return tenantsAvailable;
    }

    public Tenant getFirstTenant() {
        if (firstTenant == null) {
            TenantManagerService service = context.getService(TenantManagerService.class);
            if (service != null) {
                Iterator<Tenant> tenants = service.getTenants(context.getResolver(), null);
                if (tenants.hasNext()) {
                    firstTenant = tenants.next();
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFirstTenant({}): {}", context.getResolver().getUserID(), firstTenant);
            }
        }
        return firstTenant;
    }
}
