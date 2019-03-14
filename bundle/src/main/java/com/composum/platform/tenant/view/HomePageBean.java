package com.composum.platform.tenant.view;

import com.composum.platform.models.simple.SimpleModel;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.workflow.service.WorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

public class HomePageBean extends SimpleModel {

    private static final Logger LOG = LoggerFactory.getLogger(HomePageBean.class);

    private transient Boolean openWorkflows;
    private transient Boolean tenantsAvailable;

    public String getUserId() {
        return getResolver().getUserID();
    }

    public boolean forwardToSites() {
        if (isTenantsAvailable()) {
            Resource pagesHome = context.getResolver().getResource("/libs/composum/pages/stage/home");
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
                    LOG.error("can't forward to '{}'", pagesHome.getPath());
                }
            } else {
                LOG.warn("Pages home not available.");
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
        }
        return openWorkflows;
    }

    public boolean isTenantsAvailable() {
        if (tenantsAvailable == null) {
            TenantManagerService service = context.getService(TenantManagerService.class);
            tenantsAvailable = service != null && service.getTenants(context.getResolver(), null).hasNext();
        }
        return tenantsAvailable;
    }
}
