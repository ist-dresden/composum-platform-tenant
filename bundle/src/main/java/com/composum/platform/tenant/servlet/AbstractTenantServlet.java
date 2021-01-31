package com.composum.platform.tenant.servlet;

import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.tenant.service.impl.PlatformTenant;
import com.composum.sling.core.servlet.AbstractServiceServlet;
import com.composum.sling.core.util.ResourceUtil;
import com.composum.sling.core.util.XSS;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * the abstract tenant servlet base implementation
 */
public abstract class AbstractTenantServlet extends AbstractServiceServlet {

    public static final String PARAM_TENANT_ID = "tenant.id";
    public static final String PARAM_TENANT_NAME = "tenant.name";
    public static final String PARAM_TENANT_DESCRIPTION = "tenant.description";

    public static final String PARAM_USER_ID = "user.id";
    public static final String PARAM_ROLE = "role";

    // request utilities

    protected String getTenantId(SlingHttpServletRequest request, Resource resource, boolean mustExist) {
        String tenantId = XSS.filter(request.getParameter(PARAM_TENANT_ID));
        if (StringUtils.isBlank(tenantId)) {
            if (mustExist != ResourceUtil.isSyntheticResource(resource)) {
                tenantId = resource.getName();
            }
        }
        return tenantId;
    }

    // JSON answer

    protected interface JsonContent {
        void answer(@Nonnull final JsonWriter writer) throws IOException;
    }

    @SuppressWarnings("Duplicates")
    protected static void answer(@Nonnull final SlingHttpServletResponse response, boolean success,
                                 @Nonnull final JsonContent content)
            throws IOException {
        response.setStatus(success ? SC_OK : SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json; charset=UTF-8");
        JsonWriter writer = new JsonWriter(response.getWriter());
        writer.beginObject();
        writer.name("result").value(success ? "success" : "failure");
        content.answer(writer);
        writer.endObject();
    }

    protected static void answer(@Nonnull final JsonWriter writer, @Nonnull final String name,
                                 @Nullable final Tenant tenant, final String... status)
            throws IOException {
        if (tenant != null) {
            writer.name(name).beginObject();
            writer.name("id").value(tenant.getId());
            writer.name("name").value(tenant.getName());
            if (status.length > 0 && status[0] != null) {
                writer.name("status").value(status[0]);
            } else if (tenant instanceof PlatformTenant) {
                writer.name("status").value(((PlatformTenant) tenant).getStatus().name());
            }
            writer.endObject();
        }
    }

    protected static void answer(@Nonnull final JsonWriter writer, @Nonnull final String name,
                                 @Nullable final TenantUserManager.TenantUser user)
            throws IOException {
        if (user != null) {
            writer.name(name).beginObject();
            writer.name("id").value(user.getId());
            writer.name("name").value(user.getName());
            writer.name("roles").beginArray();
            for (TenantUserManager.Role role : user.getRoles()) {
                writer.value(role.name());
            }
            writer.endArray();
            writer.endObject();
        }
    }
}
