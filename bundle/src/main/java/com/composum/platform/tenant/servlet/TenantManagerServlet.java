package com.composum.platform.tenant.servlet;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.impl.PlatformTenant;
import com.composum.sling.core.ResourceHandle;
import com.composum.sling.core.servlet.AbstractServiceServlet;
import com.composum.sling.core.servlet.ServletOperation;
import com.composum.sling.core.servlet.ServletOperationSet;
import com.composum.sling.core.util.ResourceUtil;
import com.composum.sling.core.util.ResponseUtil;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * The servlet to provide changes of the Asset Managers UI.
 */
@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Manager Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/cpm/platform/tenants",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET,
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        })
public class TenantManagerServlet extends AbstractServiceServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TenantManagerServlet.class);

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
        tree, list, get, create, change, delete, activate
    }

    protected TenantsOperationSet operations = new TenantsOperationSet();

    protected ServletOperationSet getOperations() {
        return operations;
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    /** setup of the servlet operation set for this servlet instance */
    @Override
    public void init() throws ServletException {
        super.init();

        // GET
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.get, new GetTenantOperation());
        operations.setOperation(ServletOperationSet.Method.GET, Extension.json,
                Operation.tree, new TenantTreeOperation());

        // POST
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.create, new CreateTenantOperation());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.change, new ChangeTenantOperation());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.delete, new DeleteTenantOperation());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.json,
                Operation.activate, new ActivateTenantOperation());
    }

    public class TenantsOperationSet extends ServletOperationSet<Extension, Operation> {

        public TenantsOperationSet() {
            super(Extension.json);
        }
    }


    public class GetTenantOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            String tenantId = getTenantId(request, resource, true);
            if (StringUtils.isNotBlank(tenantId)) {
                ResourceResolver resolver = request.getResourceResolver();
                Tenant tenant = tenantManager.getTenant(resolver, tenantId);
                if (tenant != null) {
                    singleTenant(response, tenant);
                } else {
                    response.sendError(SC_BAD_REQUEST, "no tenant '" + tenantId + "' found");
                }
            } else {
                response.sendError(SC_BAD_REQUEST, "no tenant id available");
            }
        }

        protected void singleTenant(@Nonnull final SlingHttpServletResponse response, @Nonnull final Tenant tenant)
                throws IOException {
            response.setStatus(SC_OK);
            response.setContentType("application/json; charset=UTF-8");
            JsonWriter writer = new JsonWriter(response.getWriter());
            writer.beginObject();
            writer.name("id").value(tenant.getId());
            writer.name("name").value(tenant.getName());
            writer.name("description").value(tenant.getDescription());
            writer.name("properties").beginObject();
            Iterator<String> properties = tenant.getPropertyNames();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = tenant.getProperty(key);
                writer.name(key);
                jsonValue(writer, value);
            }
            writer.endObject();
            writer.endObject();
        }
    }


    //
    // Tenants 'Tree'...
    //

    protected class TenantTreeOperation implements ServletOperation {

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        @Override
        public void doIt(SlingHttpServletRequest request, SlingHttpServletResponse response,
                         ResourceHandle resource) throws IOException {
            String[] pathSegments = StringUtils.split(resource.getPath(), "/");
            switch (pathSegments.length) {
                case 3: // '/etc/tenants/{tenantId}'
                    tenantNode(request, response, pathSegments[2]);
                    break;
                default:
                    tenantsRoot(request, response);
                    break;
            }
        }

        protected void tenantsRoot(@Nonnull final SlingHttpServletRequest request,
                                   @Nonnull final SlingHttpServletResponse response)
                throws IOException {
            ResourceResolver resolver = request.getResourceResolver();
            String rootPath = tenantManager.getTenantsRoot(resolver).getPath();
            JsonWriter writer = new JsonWriter(response.getWriter());
            writer.beginObject();
            writer.name("id").value(rootPath);
            writer.name("path").value(rootPath);
            writer.name("name").value("/");
            writer.name("text").value("/");
            writer.name("type").value("root");
            writer.name("state").beginObject();
            writer.name("loaded").value(true);
            writer.endObject();
            writer.name("children").beginArray();
            int count = 0;
            Iterator<Tenant> tenants = tenantManager.getTenants(resolver, null);
            while (tenants.hasNext()) {
                Tenant tenant = tenants.next();
                String value;
                writer.beginObject();
                tenantData(resolver, writer, tenant);
                writer.endObject();
                count++;
            }
            writer.endArray();
            writer.endObject();
            if (LOG.isDebugEnabled()) {
                LOG.debug("tree.root({}): {}", rootPath, count);
            }
        }

        protected void tenantNode(@Nonnull final SlingHttpServletRequest request,
                                  @Nonnull final SlingHttpServletResponse response,
                                  @Nonnull final String tenantId)
                throws IOException {
            ResourceResolver resolver = request.getResourceResolver();
            Tenant tenant = tenantManager.getTenant(resolver, tenantId);
            if (tenant != null) {
                JsonWriter writer = new JsonWriter(response.getWriter());
                writer.beginObject();
                tenantData(resolver, writer, tenant);
                writer.name("children").beginArray();
                writer.endArray();
                writer.endObject();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("tree.node({}): {}", tenantId, tenant);
            }
        }

        protected void tenantData(@Nonnull final ResourceResolver resolver, @Nonnull final JsonWriter writer,
                                  @Nonnull final Tenant tenant)
                throws IOException {
            String rootPath = tenantManager.getTenantsRoot(resolver).getPath();
            String value;
            writer.name("id").value(rootPath + "/" + tenant.getId());
            writer.name("path").value(rootPath + "/" + tenant.getId());
            writer.name("name").value(tenant.getId());
            if (StringUtils.isBlank(value = tenant.getName())) value = tenant.getId();
            writer.name("text").value(value);
            if (tenant instanceof PlatformTenant) {
                writer.name("status").value(((PlatformTenant) tenant).getStatus().name());
            }
            writer.name("description").value(tenant.getDescription());
            writer.name("type").value("tenant");
            writer.name("state").beginObject();
            writer.name("loaded").value(true); // TODO set 'false' if tenant node has children
            writer.endObject();
        }
    }

    //
    // tenant manipulation
    //

    public class CreateTenantOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            String tenantId = getTenantId(request, resource, false);
            if (StringUtils.isNotBlank(tenantId)) {
                ResourceResolver resolver = request.getResourceResolver();
                Map<String, Object> properties = new HashMap<>();
                String value;
                if (StringUtils.isNotBlank(value = request.getParameter(PARAM_TENANT_NAME))) {
                    properties.put(PARAM_TENANT_NAME, value);
                }
                if (StringUtils.isNotBlank(value = request.getParameter(PARAM_TENANT_DESCRIPTION))) {
                    properties.put(PARAM_TENANT_DESCRIPTION, value);
                }
                try {
                    Tenant tenant = tenantManager.createTenant(resolver, tenantId, properties);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("create({}): {}", tenantId, tenant);
                    }
                    answer(response, true, tenant, null);
                } catch (PersistenceException ex) {
                    LOG.error(ex.toString(), ex);
                    response.sendError(SC_BAD_REQUEST, "can't create tenant '" + tenantId + "': "
                            + ResponseUtil.getMessage(ex));
                }
            } else {
                response.sendError(SC_BAD_REQUEST, "no id for a new tenant found");
            }
        }
    }

    public class ChangeTenantOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            String tenantId = getTenantId(request, resource, true);
            if (StringUtils.isNotBlank(tenantId)) {
                ResourceResolver resolver = request.getResourceResolver();
                Tenant tenant = tenantManager.getTenant(resolver, tenantId);
                if (tenant != null) {
                    Map<String, Object> properties = new HashMap<>();
                    for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
                        String key = parameter.getKey();
                        if (key.startsWith("p.")) {
                            String[] value = parameter.getValue();
                            properties.put(key.substring(2), value != null && value.length == 1 ? value[0] : value);
                        }
                    }
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("change({}): {}", tenant.getId(), tenant);
                        }
                        tenantManager.changeTenant(resolver, tenantId, properties);
                        answer(response, true, tenantManager.getTenant(resolver, tenantId), tenant);
                    } catch (PersistenceException ex) {
                        LOG.error(ex.toString(), ex);
                        response.sendError(SC_BAD_REQUEST, "can't change tenant '" + tenantId + "': "
                                + ResponseUtil.getMessage(ex));
                    }
                } else {
                    response.sendError(SC_BAD_REQUEST, "no tenant '" + tenantId + "' found");
                }
            } else {
                response.sendError(SC_BAD_REQUEST, "no tenant id available");
            }
        }
    }

    public class DeleteTenantOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            String tenantId = getTenantId(request, resource, true);
            if (StringUtils.isNotBlank(tenantId)) {
                ResourceResolver resolver = request.getResourceResolver();
                Tenant tenant = tenantManager.getTenant(resolver, tenantId);
                if (tenant != null) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("delete({}): {}", tenant.getId(), tenant);
                        }
                        tenantManager.deleteTenant(resolver, tenantId);
                        answer(response, true, tenantManager.getTenant(resolver, tenantId), tenant);
                    } catch (PersistenceException ex) {
                        LOG.error(ex.toString(), ex);
                        response.sendError(SC_BAD_REQUEST, "can't delete tenant '" + tenantId + "': "
                                + ResponseUtil.getMessage(ex));
                    }
                } else {
                    response.sendError(SC_BAD_REQUEST, "no tenant '" + tenantId + "' found");
                }
            } else {
                response.sendError(SC_BAD_REQUEST, "no tenant id available");
            }
        }
    }

    public class ActivateTenantOperation implements ServletOperation {

        @Override
        public void doIt(@Nonnull final SlingHttpServletRequest request,
                         @Nonnull final SlingHttpServletResponse response,
                         @Nonnull final ResourceHandle resource)
                throws IOException {
            String tenantId = getTenantId(request, resource, true);
            if (StringUtils.isNotBlank(tenantId)) {
                ResourceResolver resolver = request.getResourceResolver();
                Tenant tenant = tenantManager.getTenant(resolver, tenantId);
                if (tenant != null) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("activate({}): {}", tenant.getId(), tenant);
                        }
                        tenantManager.reanimateTenant(resolver, tenantId);
                        answer(response, true, tenantManager.getTenant(resolver, tenantId), tenant);
                    } catch (PersistenceException ex) {
                        LOG.error(ex.toString(), ex);
                        response.sendError(SC_BAD_REQUEST, "can't activate tenant '" + tenantId + "': "
                                + ResponseUtil.getMessage(ex));
                    }
                } else {
                    response.sendError(SC_BAD_REQUEST, "no tenant '" + tenantId + "' found");
                }
            } else {
                response.sendError(SC_BAD_REQUEST, "no tenant id available");
            }
        }
    }

    // request utilities

    protected String getTenantId(SlingHttpServletRequest request, Resource resource, boolean mustExist) {
        String tenantId = request.getParameter(PARAM_TENANT_ID);
        if (StringUtils.isBlank(tenantId)) {
            if (mustExist != ResourceUtil.isSyntheticResource(resource)) {
                tenantId = resource.getName();
            }
        }
        return tenantId;
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
        if (tenant != null) {
            writer.name("tenant").beginObject();
            writer.name("id").value(tenant.getId());
            writer.name("name").value(tenant.getName());
            if (tenant instanceof PlatformTenant) {
                writer.name("status").value(((PlatformTenant) tenant).getStatus().name());
            }
            writer.endObject();
        } else if (before != null) {
            writer.name("tenant").beginObject();
            writer.name("id").value(before.getId());
            writer.name("name").value(before.getName());
            writer.name("status").value("removed");
            writer.endObject();
        }
        writer.endObject();
    }
}
