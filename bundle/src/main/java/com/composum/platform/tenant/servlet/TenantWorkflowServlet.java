package com.composum.platform.tenant.servlet;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.sling.core.ResourceHandle;
import com.composum.sling.core.servlet.AbstractServiceServlet;
import com.composum.sling.core.servlet.ServletOperation;
import com.composum.sling.core.servlet.ServletOperationSet;
import com.google.gson.stream.JsonWriter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * The servlet to provide changes of the Asset Managers UI.
 */
@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Workflow Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/cpm/platform/tenants/workflow",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET,
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        })
public class TenantWorkflowServlet extends AbstractServiceServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TenantWorkflowServlet.class);

    public static final String PARAM_TENANT_ID = "tenant.id";
    public static final String PARAM_TENANT_NAME = "tenant.name";
    public static final String PARAM_TENANT_DESCRIPTION = "tenant.description";

    @Reference
    protected TenantManagerService tenantManager;

    protected BundleContext bundleContext;

    @Activate
    @Modified
    protected void activate(ComponentContext context) {
        this.bundleContext = context.getBundleContext();
    }

    //
    // Servlet operations
    //

    public enum Extension {
        html, json
    }

    public enum Operation {
        inbox, joinme
    }

    protected TenantsOperationSet operations = new TenantsOperationSet();

    protected ServletOperationSet getOperations() {
        return operations;
    }

    @Deprecated
    protected boolean isEnabled() {
        return true;
    }

    /** setup of the servlet operation set for this servlet instance */
    @Override
    public void init() throws ServletException {
        super.init();

        // GET
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.inbox, new InboxOperation());

        // POST
    }

    public class TenantsOperationSet extends ServletOperationSet<Extension, Operation> {

        public TenantsOperationSet() {
            super(Extension.json);
        }
    }


    public class InboxOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
        }
    }

    // JSON answer

    protected void answer(@Nonnull final SlingHttpServletResponse response, boolean success,
                          @Nullable final Tenant tenant, @Nullable final Tenant before)
            throws IOException {
        response.setStatus(success ? SC_OK : SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json; charset=UTF-8");
        JsonWriter writer = new JsonWriter(response.getWriter());
        writer.beginObject();
        writer.name("result").value(success ? "success" : "failure");
        writer.endObject();
    }
}
