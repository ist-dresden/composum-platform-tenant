package com.composum.platform.tenant.servlet;

import com.composum.platform.tenant.service.HostManagerService;
import com.composum.platform.tenant.service.HostManagerService.Host;
import com.composum.platform.tenant.service.HostManagerService.ProcessException;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.ResourceHandle;
import com.composum.sling.core.servlet.ServletOperation;
import com.composum.sling.core.servlet.ServletOperationSet;
import com.composum.sling.core.servlet.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
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

import static com.composum.platform.tenant.service.HostManagerService.VALUE_ADDRESS;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_CERTIFICATE;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_CONFIGURED;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_ENABLED;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_HOSTNAME;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_LOCKED;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_SECURED;
import static com.composum.platform.tenant.service.HostManagerService.VALUE_VALID;

/**
 * The servlet to provide changes of the Asset Managers UI.
 */
@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Host Manager Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/cpm/platform/tenants/host",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET,
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        })
public class HostManagerServlet extends AbstractTenantServlet {

    private static final Logger LOG = LoggerFactory.getLogger(HostManagerServlet.class);

    public static final String PARAM_HOSTNAME = "hostname";
    public static final String PARAM_SITE = "site";
    public static final String PARAM_STAGE = "stage";

    public static final String LIST_HOSTS = "hosts";
    public static final String DATA_HOST = "host";

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
        json
    }

    public enum Operation {
        list, status,
        // tenant related
        add, remove,
        // site assignment
        assign,
        // hosts configuration
        create, enable, disable, cert, revoke, secure, unsecure, delete
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
                Operation.assign, new AssignSite());

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
                Operation.unsecure, new UnsecureHost());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.delete, new DeleteHost());

        // POST
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.add, new AddHost());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.remove, new RemoveHost());

        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.assign, new AssignSite());

        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.create, new CreateHost());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.enable, new EnableHost());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.disable, new DisableHost());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.cert, new HostCertificate());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.revoke, new RevokeCertificate());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.secure, new SecureHost());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.unsecure, new UnsecureHost());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
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
            String tenantId = getTenantId(request, resource, true);
            List<Map<String, Object>> result = status.list(LIST_HOSTS);
            try {
                for (Host host : hostManager.hostList(resolver, tenantId)) {
                    result.add(new HashMap<String, Object>() {{
                        put(VALUE_HOSTNAME, host.getHostname());
                        put(VALUE_CONFIGURED, host.isConfigured());
                        put(VALUE_LOCKED, host.isLocked());
                        put(VALUE_ENABLED, host.isEnabled());
                        put(VALUE_CERTIFICATE, host.isCertAvailable());
                        put(VALUE_SECURED, host.isSecured());
                        put(VALUE_ADDRESS, host.getAddress());
                        put(VALUE_VALID, host.isValid());
                    }});
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
            String tenantId = getTenantId(request, resource, true);
            String hostname = request.getParameter(PARAM_HOSTNAME);
            if (StringUtils.isNotBlank(hostname)) {
                try {
                    Host host = perform(request, response, status, resolver, tenantId, hostname);
                    hostData(status, host);
                } catch (PersistenceException ex) {
                    status.withLogging(LOG).error("processing error: {}", ex.getMessage());
                } catch (ProcessException ex) {
                    status.withLogging(LOG).error("processing error ({}): {}",
                            ex.getExitValue(), StringUtils.join(ex.getErrorMessages(), ", "));
                }
            } else {
                status.withLogging(LOG).error("no 'hostname' parameter found");
            }
            status.sendJson();
        }

        protected void hostData(Status status, Host host) {
            if (host != null) {
                Map<String, Object> data = status.data(DATA_HOST);
                data.put(VALUE_HOSTNAME, host.getHostname());
                data.put(VALUE_CONFIGURED, host.isConfigured());
                data.put(VALUE_LOCKED, host.isLocked());
                data.put(VALUE_ENABLED, host.isEnabled());
                data.put(VALUE_CERTIFICATE, host.isCertAvailable());
                data.put(VALUE_SECURED, host.isSecured());
                data.put(VALUE_ADDRESS, host.getAddress());
                data.put(VALUE_VALID, host.isValid());
            }
        }

        protected Host perform(@Nonnull final SlingHttpServletRequest request,
                               @Nonnull final SlingHttpServletResponse response,
                               @Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException, PersistenceException {
            return perform(status, resolver, tenantId, hostname);
        }

        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return null;
        }
    }

    public class HostStatus extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostStatus(resolver, tenantId, hostname);
        }
    }

    // tenant hosts management

    public class AddHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            try {
                if (StringUtils.isBlank(tenantId)) {
                    throw new ProcessException("no tenant specified");
                }
                return hostManager.addHost(resolver, tenantId, hostname);
            } catch (PersistenceException ex) {
                status.withLogging(LOG).error(ex.getMessage(), ex);
                return null;
            }
        }
    }

    public class RemoveHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            try {
                if (StringUtils.isBlank(tenantId)) {
                    throw new ProcessException("no tenant specified");
                }
                hostManager.removeHost(resolver, tenantId, hostname);
            } catch (PersistenceException ex) {
                status.withLogging(LOG).error(ex.getMessage(), ex);
            }
            return null;
        }
    }

    // site assignment

    public class AssignSite extends HostOperation {

        @Override
        protected Host perform(@Nonnull final SlingHttpServletRequest request,
                               @Nonnull final SlingHttpServletResponse response,
                               @Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException, PersistenceException {
            if (StringUtils.isBlank(tenantId)) {
                throw new ProcessException("no tenant specified");
            }
            String sitePath = request.getParameter(PARAM_SITE);
            String siteStage = request.getParameter(PARAM_STAGE);
            BeanContext context = new BeanContext.Servlet(getServletContext(), bundleContext, request, response);
            return hostManager.assignSite(context, tenantId, hostname, sitePath, siteStage);
        }
    }

    // server host configuration

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

    public class UnsecureHost extends HostOperation {

        @Override
        protected Host perform(@Nonnull final Status status, @Nonnull final ResourceResolver resolver,
                               @Nullable final String tenantId, @Nonnull final String hostname)
                throws ProcessException {
            return hostManager.hostUnsecure(resolver, tenantId, hostname);
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
