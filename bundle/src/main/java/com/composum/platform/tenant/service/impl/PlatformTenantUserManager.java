package com.composum.platform.tenant.service.impl;

import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.sling.core.service.PermissionsService;
import com.composum.sling.platform.security.PlatformAccessService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant User Manager"
        },
        immediate = true
)
public class PlatformTenantUserManager extends AbstractTenantService implements TenantUserManager {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTenantUserManager.class);

    private static final String TENANT_GROUP_PREFIX = "tenant-";

    @Reference
    protected void setResolverFactory(ResourceResolverFactory factory){
        resolverFactory = factory;
    }

    @Reference
    protected void setPlatformAccessService(PlatformAccessService service){
        accessService = service;
    }

    @Reference
    protected void setPermissionsService(PermissionsService service){
        permissionsService = service;
    }

    @Override
    @Nonnull
    public Collection<UserInfo> getTenantUsers(@Nonnull final ResourceResolver resolver,
                                               @Nonnull final String tenantId) {
        List<UserInfo> users = new ArrayList<>();
        return users;
    }

    @Override
    public void assign(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                       @Nonnull final String userId, @Nonnull final String... roles)
            throws RepositoryException {
        call((session, context) -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("assign roles({},{},[{}])...", tenantId, userId, StringUtils.join(roles, ","));
            }
            changeGroup(session, tenantId, Group::addMember, userId, roles);
            return null;
        }, resolver, tenantId);
    }

    @Override
    public void revoke(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                       @Nonnull final String userId, @Nonnull final String... roles)
            throws RepositoryException {
        call((session, context) -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("revoke roles({},{},[{}])...", tenantId, userId, StringUtils.join(roles, ","));
            }
            changeGroup(session, tenantId, Group::removeMember, userId, roles);
            return null;
        }, resolver, tenantId);
    }

    @Override
    public void define(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                       @Nonnull final String userId, @Nonnull final String... roles)
            throws RepositoryException {
        call((session, context) -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("define roles({},{},[{}])...", tenantId, userId, StringUtils.join(roles, ","));
            }
            List<String> toAssign = Arrays.asList(roles);
            List<String> toRevoke = new ArrayList<>();
            for (Role role : Role.values()) {
                if (!toAssign.contains(role.name())) {
                    toRevoke.add(role.name());
                }
            }
            changeGroup(session, tenantId, Group::addMember, userId, roles);
            changeGroup(session, tenantId, Group::removeMember, userId, toRevoke.toArray(new String[0]));
            return null;
        }, resolver, tenantId);
    }

    // group change methods

    protected String getGroupId(@Nonnull final String tenantId, Role role) {
        return TENANT_GROUP_PREFIX + tenantId + "-" + role.name() + "s";
    }

    protected interface GroupOp {
        void change(Group group, User user) throws RepositoryException;
    }

    protected void changeGroup(@Nonnull final JackrabbitSession session, @Nonnull final String tenantId,
                               @Nonnull final GroupOp op, @Nonnull final String userId, String[] roles)
            throws RepositoryException {
        final UserManager userManager = session.getUserManager();
        Authorizable authorizable = userManager.getAuthorizable(userId);
        if (authorizable instanceof User) {
            User user = (User) authorizable;
            for (String key : roles) {
                Role role = Role.valueOf(key);
                authorizable = userManager.getAuthorizable(getGroupId(tenantId, role));
                if (authorizable instanceof Group) {
                    Group group = (Group) authorizable;
                    op.change(group, user);
                } else {
                    throw new IllegalArgumentException("tenant group for '" + role + "' not available");
                }
            }
        } else {
            throw new IllegalArgumentException("user '" + userId + "' not available");
        }
    }

    // service driven execution

    /**
     * call action using service resolver if requested access is granted...
     */
    protected <T> T call(@Nonnull final ServiceSessionTask<T> task,
                         @Nonnull ResourceResolver context, @Nonnull String tenantId)
            throws RepositoryException {
        T result;
        JackrabbitSession session = (JackrabbitSession) context.adaptTo(Session.class);
        if (session != null) {
            if (permissionsService.isMemberOfAll(session, getGroupId(tenantId, Role.manager))) {
                try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("access granted, using service resolver ({})...", context.getUserID());
                    }
                    session = (JackrabbitSession) serviceResolver.adaptTo(Session.class);
                    if (session != null) {
                        result = task.call(session, context);
                        session.save();
                    } else {
                        throw new RepositoryException("can't adapt to session");
                    }
                } catch (LoginException ex) {
                    LOG.error(ex.getMessage(), ex);
                    throw new RepositoryException(ex.getMessage(), ex);
                }
            } else {
                throw new RepositoryException("insufficient permissions (" + context.getUserID() + ")");
            }
        } else {
            throw new RepositoryException("can't adapt to session");
        }
        return result;
    }
}