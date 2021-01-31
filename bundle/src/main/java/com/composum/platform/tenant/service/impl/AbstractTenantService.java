package com.composum.platform.tenant.service.impl;

import com.composum.sling.core.service.PermissionsService;
import com.composum.sling.platform.security.PlatformAccessService;
import com.composum.sling.platform.security.PlatformAccessService.AccessContext;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;

public class AbstractTenantService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTenantService.class);

    /* to set up by the derived class */
    protected ResourceResolverFactory resolverFactory;
    protected PermissionsService permissionsService;
    protected PlatformAccessService accessService;

    //
    // to implement the interface methods driven in various resolver contexts
    //

    protected interface PermissionCheck {

        /**
         * returns 'true' if requested access is granted
         *
         * @param resolver the resolver to use (context resolver) fpr permission check
         */
        boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId);
    }

    protected interface ResourceResolverTask<T> {

        /**
         * performs an operation using the specified resolver (service resolver) honoring the context resolver
         * (requests resolver); the 'resolver' is used to retrieve and change the resources; the 'context' is used
         * to build the result (probably restricted to public data)
         *
         * @param resolver the (service) resolver to perform the task
         * @param context  the (request) resolver to produce the result (probably restricted)
         */
        T call(@Nonnull ResourceResolver resolver, @Nonnull ResourceResolver context) throws PersistenceException;
    }

    protected interface ServiceSessionTask<T> {

        /**
         * performs an operation using the specified session (service resolver) honoring the context resolver
         * (requests resolver); the 'session' is used to retrieve and change the resources; the 'context' is used
         * to build the result (probably restricted to public data)
         *
         * @param session the (service) session to perform the task
         * @param context the (request) resolver to produce the result (probably restricted)
         */
        T call(@Nonnull JackrabbitSession session, @Nonnull ResourceResolver context) throws RepositoryException;
    }

    /**
     * call using service resolver (without permission check!)...
     */
    protected <T> T call(@Nonnull final ResourceResolverTask<T> task, @Nullable ResourceResolver context) {
        T result = null;
        try {
            if (context == null) {
                context = getAccessContextResolver();
            }
            try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
                result = task.call(serviceResolver, context);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * call action using service resolver if requested access is granted otherwise the given resolver...
     */
    protected <T> T call(@Nonnull final ResourceResolverTask<T> task, @Nullable PermissionCheck permissionCheck,
                         @Nullable ResourceResolver context, @Nullable String tenantId)
            throws PersistenceException {
        T result;
        try {
            if (context == null) {
                context = getAccessContextResolver();
            }
            if (permissionCheck == null || permissionCheck.isAccessGranted(context, tenantId)) {
                try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("access granted, using service resolver ({})...", context.getUserID());
                    }
                    result = task.call(serviceResolver, context);
                    serviceResolver.commit();
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("access NOT granted! using request resolver ({})...", context.getUserID());
                }
                result = task.call(context, context);
                context.commit();
            }
        } catch (IllegalAccessException | LoginException ex) {
            throw new PersistenceException(ex.getMessage(), ex);
        }
        return result;
    }

    protected ResourceResolver getAccessContextResolver() throws IllegalAccessException {
        AccessContext accessContext = accessService.getAccessContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieving resolver from access context: {}", accessContext);
        }
        if (accessContext != null) {
            return accessContext.getResolver();
        } else {
            throw new IllegalAccessException("can't call resolver from access service");
        }
    }
}
