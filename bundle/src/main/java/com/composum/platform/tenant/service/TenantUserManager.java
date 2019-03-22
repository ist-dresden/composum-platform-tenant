package com.composum.platform.tenant.service;

import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface TenantUserManager {

    void define(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                @Nonnull String userId, @Nonnull String... roles)
            throws RepositoryException;

    enum Role {visitor, editor, publisher, developer, manager}

    class UserInfo {

        private String userId;
        private String name;
        private List<Role> roles;

        public UserInfo(String userId, String name, Role... roles) {
            this.userId = userId;
            this.name = name;
            this.roles = Arrays.asList(roles);
        }
    }

    @Nonnull
    public Collection<UserInfo> getTenantUsers(@Nonnull ResourceResolver resolver,
                                               @Nonnull String tenantId);

    void assign(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                @Nonnull String userId, @Nonnull String... roles)
            throws RepositoryException;

    void revoke(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                @Nonnull String userId, @Nonnull String... roles)
            throws RepositoryException;
}
