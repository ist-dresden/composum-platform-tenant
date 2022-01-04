package com.composum.platform.tenant.servlet;

import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.sling.core.ResourceHandle;
import com.composum.sling.core.servlet.ServletOperation;
import com.composum.sling.core.servlet.ServletOperationSet;
import com.composum.sling.core.util.RequestUtil;
import com.composum.sling.core.util.ResponseUtil;
import com.composum.sling.core.util.XSS;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
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
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * The servlet to provide changing the user role assignments.
 */
@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Manager Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/cpm/platform/tenants/user",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET,
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        })
public class TenantUserServlet extends AbstractTenantServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TenantUserServlet.class);

    @Reference
    protected TenantUserManager userManager;

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
        json
    }

    public enum Operation {
        change
    }

    protected TenantUsersOperationSet operations = new TenantUsersOperationSet();

    protected ServletOperationSet getOperations() {
        return operations;
    }

    @Deprecated
    protected boolean isEnabled() {
        return true;
    }

    @Override
    public void init() throws ServletException {
        super.init();

        // POST
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.change, new ChangeUserOperation());
    }

    public class TenantUsersOperationSet extends ServletOperationSet<Extension, Operation> {

        public TenantUsersOperationSet() {
            super(Extension.json);
        }
    }

    //
    // user manipulation
    //

    public class ChangeUserOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            String tenantId = getTenantId(request, resource, true);
            if (StringUtils.isNotBlank(tenantId)) {
                String userId = RequestUtil.getParameter(request, PARAM_USER_ID, "");
                if (StringUtils.isNotBlank(userId)) {
                    try {
                        String[] roles = XSS.filter(request.getParameterValues(PARAM_ROLE));
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("change({},{}): {}", tenantId, userId, StringUtils.join(roles, ", "));
                        }
                        ResourceResolver resolver = request.getResourceResolver();
                        userManager.define(resolver, tenantId, userId, roles != null ? roles : new String[0]);
                        answer(response, true, writer -> {

                        });
                    } catch (RepositoryException ex) {
                        LOG.error(ex.toString(), ex);
                        response.sendError(SC_BAD_REQUEST, "can't change roles of user '" + userId + "': "
                                + ResponseUtil.getMessage(ex));
                    }
                } else {
                    response.sendError(SC_BAD_REQUEST, "no user id specified");
                }
            } else {
                response.sendError(SC_BAD_REQUEST, "no tenant id available");
            }
        }
    }
}
