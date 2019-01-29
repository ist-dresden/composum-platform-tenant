package com.composum.platform.tenant.service;

import com.composum.sling.core.filter.ResourceFilter;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

/**
 * a tenant management service definition using a resolver context (user session) for each operation
 * (mapped to service user if the access to perform the requested operation is granted)
 */
public interface TenantManagerService {

    /**
     * @param resolver the resolver of the users session (request resolver)
     * @return the root resource of the managed tenant resources
     */
    @Nonnull
    Resource getTenantsRoot(@Nonnull ResourceResolver resolver);

    @Nullable
    Tenant getTenant(@Nonnull ResourceResolver resolver, @Nonnull String tenantId);

    /**
     * returns the tenant list using the given filter as scope
     */
    @Nonnull
    Iterator<Tenant> getTenants(@Nonnull ResourceResolver resolver, @Nullable ResourceFilter filter);

    /**
     * will create a new tenant (mustn't be existing)
     */
    @Nonnull
    Tenant createTenant(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                        @Nullable Map<String, Object> properties)
            throws PersistenceException;

    /**
     * is changing the given properties an reapplying the tenant template
     */
    void changeTenant(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                      @Nonnull Map<String, Object> properties)
            throws PersistenceException;

    /**
     * deactivate the tenant (instead of removal); this is changing the status only
     */
    void deactivateTenant(@Nonnull ResourceResolver resolver, @Nonnull String tenantId)
            throws PersistenceException;

    /**
     * reactivate a deactivated tenant; this is also changing the status only
     */
    void reanimateTenant(@Nonnull ResourceResolver resolver, @Nonnull String tenantId)
            throws PersistenceException;

    /**
     * will delete the tenant finally (!)
     */
    void deleteTenant(@Nonnull ResourceResolver resolver, @Nonnull String tenantId)
            throws PersistenceException;
}
