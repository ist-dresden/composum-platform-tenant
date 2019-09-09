package com.composum.platform.tenant.servlet;

import com.composum.platform.tenant.service.HostManagerService;
import com.composum.platform.tenant.service.HostManagerService.Host;
import com.composum.platform.tenant.service.HostManagerService.ProcessException;
import com.composum.sling.core.ResourceHandle;
import com.composum.sling.core.servlet.ServletOperation;
import com.composum.sling.core.servlet.ServletOperationSet;
import com.composum.sling.core.servlet.Status;
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
import javax.annotation.Nullable;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The servlet to provide changes of the Asset Managers UI.
 */
@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Host Manager Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/cpm/platform/tenants/host",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET
        })
public class HostManagerServlet extends AbstractTenantServlet {

    private static final Logger LOG = LoggerFactory.getLogger(HostManagerServlet.class);

    @Reference
    protected HostManagerService hostManager;

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
        list, status, create, enable, disable, cert, revoke, secure, delete
    }

    protected HostsOperationSet operations = new HostsOperationSet();

    protected ServletOperationSet getOperations() {
        return operations;
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    /**
     * setup of the servlet operation set for this servlet instance
     */
    @Override
    public void init() throws ServletException {
        super.init();

        // GET
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.list, new ListHosts());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.status, new HostStatus());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.create, new CreateHost());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.enable, new EnableHost());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.disable, new DisableHost());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.cert, new HostCertificate());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.revoke, new RevokeCertificate());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.secure, new SecureHost());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.delete, new DeleteHost());
    }

    public class HostsOperationSet extends ServletOperationSet<Extension, Operation> {

        public HostsOperationSet() {
            super(Extension.json);
        }
    }

    public class ListHosts implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            Status status = new Status(request, response);
            ResourceResolver resolver = request.getResourceResolver();
            String tenantId = request.getParameter("tenant");
            List<Map<String, Object>> result = status.list("hosts");
            try {
                for (Host host : hostManager.hostList(resolver, tenantId)) {
                    Map<String, Object> item = new HashMap<String, Object>() {{
                        put("hostname", host.getHostname());
                        put("enabled", host.isEnabled());
                        put("certificate", host.isCertAvailable());
                        put("secured", host.isSecured());
                        put("address", host.getInetAddress());
                        put("valid", host.isValid());
                    }};
                    result.add(item);
                }
            } catch (ProcessException ex) {
                status.withLogging(LOG).error("processing error ({}): {}",
                        ex.getExitValue(), StringUtils.join(ex.getErrorMessages(), ", "));
            }
            status.sendJson();
        }
    }

    protected abstract class HostOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            Status status = new Status(request, response);
            ResourceResolver resolver = request.getResourceResolver();
            String tenantId = request.getParameter("tenant");
            String hostname = request.getParameter("hostname");
            if (StringUtils.isNotBlank(hostname)) {
                try {
                    Host host = perform(status, resolver, tenantId, hostname);
                    if (host != null) {
                        Map<String, Object> data = status.data("host");
                        data.put("hostname", host.getHostname());
                        data.put("enabled", host.isEnabled());
                        data.put("cert", host.isCertAvailable());
                        data.put("secured", host.isSecured());
                        data.put("address", host.getInetAddress());
                        data.put("valid", host.isValid());
                    } else {
                        status.withLogging(LOG).warn("host not found - '{}'", hostname);
                    }
                } catch (ProcessException ex) {
                    status.withLogging(LOG).error("processing error ({}): {}",
                            ex.getExitValue(), StringUtils.join(ex.getErrorMessages(), ", "));
                }
            } else {
                status.withLogging(LOG).error("no 'hostname' parameter found");
            }
            status.sendJson();
        }

        protected abstract Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                                        @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException;
    }

    public class HostStatus extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostStatus(resolver, tenantId, hostname);
        }
    }

    public class CreateHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostCreate(resolver, tenantId, hostname);
        }
    }

    public class EnableHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostEnable(resolver, tenantId, hostname);
        }
    }

    public class DisableHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostDisable(resolver, tenantId, hostname);
        }
    }

    public class HostCertificate extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostCert(resolver, tenantId, hostname);
        }
    }

    public class RevokeCertificate extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostRevoke(resolver, tenantId, hostname);
        }
    }

    public class SecureHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostSecure(resolver, tenantId, hostname);
        }
    }

    public class DeleteHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            hostManager.hostDelete(resolver, tenantId, hostname);
            return null;
        }
    }
}
