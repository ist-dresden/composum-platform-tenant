package com.composum.platform.tenant.service;

import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public interface TenantUserManager {

    enum Role {
        member,     /* implicit role/group */
        visitor,    /* joined to the tenant but NOT a member */
        publisher,  /* member, can publish content */
        editor,     /* member, can manipulate the content */
        developer,  /* member, can modify the components of the tenant */
        manager,    /* member, can manage the tenant and the tenants users */
        assistant   /* can read everything but not change; is NOT a member */
    }

    interface TenantUser extends Comparable<TenantUser> {

        @Nonnull
        String getId();

        String getName();

        String getEmail();

        boolean isVisitor();

        boolean isPublisher();

        boolean isEditor();

        boolean isDeveloper();

        boolean isManager();

        boolean isAssistant();

        boolean hasRole(Role role);

        @Nonnull
        List<Role> getRoles();
    }

    class TenantUsers extends ArrayList<TenantUser> {

        public int getCount() {
            return size();
        }
    }

    boolean isInRole(@Nonnull String tenantId, @Nonnull Role role, @Nonnull String userId);

    /**
     * @return the user object if the user is joined to the tenant otherwise 'null'
     */
    @Nullable
    TenantUser getTenantUser(@Nonnull ResourceResolver resolver, @Nonnull String tenantId, @Nonnull String userId)
            throws RepositoryException;

    /**
     * @return the set of users joined to the tenant
     */
    @Nonnull
    TenantUsers getTenantUsers(@Nonnull ResourceResolver resolver, @Nonnull String tenantId)
            throws RepositoryException;

    /**
     * assigns a set of roles to a user (adds the roles to the set of assigned roles)
     */
    void assign(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                @Nonnull String userId, @Nonnull String... roles)
            throws RepositoryException;

    /**
     * replaces a set of roles for a user
     */
    void define(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                @Nonnull String userId, @Nonnull String... roles)
            throws RepositoryException;

    /**
     * removes a set of roles for a user
     */
    void revoke(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                @Nonnull String userId, @Nonnull String... roles)
            throws RepositoryException;
}
