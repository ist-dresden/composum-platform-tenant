package com.composum.platform.tenant.service.impl;

import com.composum.platform.tenant.service.PlatformTenantHook;
import com.composum.sling.core.service.RepositorySetupService;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * add / remove tenant folders and groups according to the tenant management operations
 */
@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Content Hook"
        },
        immediate = true
)
public class PlatformTenantManagerHook implements PlatformTenantHook {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTenantManagerHook.class);

    private static final String TENANT_ACLS = "/conf/composum/platform/tenant/acl/tenant.json";
    private static final String DEACTIVATED_ACLS = "/conf/composum/platform/tenant/acl/deactivated.json";

    @Reference
    private RepositorySetupService setupService;

    @Override
    public Map<String, Object> setup(@Nonnull final ResourceResolver resolver,
                                     @Nonnull final ResourceResolver context,
                                     @Nonnull final Tenant t)
            throws PersistenceException {
        return change(resolver, context, t);
    }

    @Override
    public Map<String, Object> change(@Nonnull final ResourceResolver resolver,
                                      @Nonnull final ResourceResolver context,
                                      @Nonnull final Tenant t)
            throws PersistenceException {
        final PlatformTenant tenant = (PlatformTenant) t;
        try {
            String aclRules = tenant.isActive() ? TENANT_ACLS : DEACTIVATED_ACLS;
            if (LOG.isDebugEnabled()) {
                LOG.debug("applying ACL rules: {}", aclRules);
            }
            //noinspection ConstantConditions
            setupService.addJsonAcl(resolver.adaptTo(Session.class), aclRules, getTenantValues(tenant));
        } catch (RepositoryException | IOException ex) {
            throw new PersistenceException(ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void remove(@Nonnull final ResourceResolver resolver,
                       @Nonnull final ResourceResolver context,
                       @Nonnull final Tenant t)
            throws PersistenceException {
        final PlatformTenant tenant = (PlatformTenant) t;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("removing ACL rules: {}", TENANT_ACLS);
            }
            //noinspection ConstantConditions
            setupService.removeJsonAcl(resolver.adaptTo(Session.class), TENANT_ACLS, getTenantValues(tenant));
        } catch (RepositoryException | IOException ex) {
            throw new PersistenceException(ex.getMessage(), ex);
        }
    }

    protected Map<String, String> getTenantValues(@Nonnull final PlatformTenant tenant) {
        return new HashMap<String, String>() {{
            put("tenantId", tenant.getId());
            put("contentRoot", tenant.getContentRoot());
            put("applicationRoot", tenant.getApplicationRoot());
            put("principalBase", tenant.getPrincipalBase());
        }};
    }
}
