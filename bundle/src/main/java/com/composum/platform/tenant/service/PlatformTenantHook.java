package com.composum.platform.tenant.service;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * a TenantManagerHook working in an explicit resolver context provided by the manager service
 */
public interface PlatformTenantHook {

    /**
     * @param resolver the resolver to use for changes (the resolver of the service user)
     * @param context  the resolver of the request which has triggered the change (the users session)
     * @return the properties to change built by the hook
     */
    Map<String, Object> setup(@Nonnull ResourceResolver resolver,
                              @Nonnull ResourceResolver context,
                              @Nonnull Tenant tenant)
            throws PersistenceException;

    /**
     * @param resolver the resolver to use for changes (the resolver of the service user)
     * @param context  the resolver of the request which has triggered the change (the users session)
     * @return the properties to change built by the hook
     */
    Map<String, Object> change(@Nonnull ResourceResolver resolver,
                               @Nonnull ResourceResolver context,
                               @Nonnull Tenant tenant)
            throws PersistenceException;

    /**
     * @param resolver the resolver to use for changes (the resolver of the service user)
     * @param context  the resolver of the request which has triggered the change (the users session)
     */
    void remove(@Nonnull ResourceResolver resolver,
                @Nonnull ResourceResolver context,
                @Nonnull Tenant tenant)
            throws PersistenceException;
}
