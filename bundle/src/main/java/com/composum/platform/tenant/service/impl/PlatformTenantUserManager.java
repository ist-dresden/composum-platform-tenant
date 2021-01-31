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
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant User Manager"
        },
        immediate = true
)
public class PlatformTenantUserManager extends AbstractTenantService implements TenantUserManager {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTenantUserManager.class);

    public static final String PN_LAST_LOGIN = "lastLogin";

    private static final String TENANT_GROUP_PREFIX = "tenant-";

    private static final Role[] TENANT_USER_ROLES =
            new Role[]{Role.visitor, Role.publisher, Role.editor, Role.developer, Role.manager};

    private class PlatformTenantUser implements TenantUser {

        private String userId;
        private String name;
        private String email;
        private Calendar lastLogin;
        private List<Role> roles;

        public PlatformTenantUser(@Nonnull final String userId,
                                  @Nullable final String name, @Nonnull final String email,
                                  @Nullable final Calendar lastLogin,
                                  @Nonnull final Role... roles) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.lastLogin = lastLogin;
            this.roles = Arrays.asList(roles);
        }

        @Override
        @Nonnull
        public String getId() {
            return userId;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        @Nullable
        public Calendar getLastLogin() {
            return lastLogin;
        }

        @Override
        public boolean isVisitor() {
            return hasRole(Role.visitor);
        }

        @Override
        public boolean isPublisher() {
            return hasRole(Role.publisher);
        }

        @Override
        public boolean isEditor() {
            return hasRole(Role.editor);
        }

        @Override
        public boolean isDeveloper() {
            return hasRole(Role.developer);
        }

        @Override
        public boolean isManager() {
            return hasRole(Role.manager);
        }

        @Override
        public boolean isAssistant() {
            return hasRole(Role.assistant);
        }

        @Override
        public boolean hasRole(Role role) {
            return roles.contains(role);
        }

        @Override
        @Nonnull
        public List<Role> getRoles() {
            return roles;
        }

        @Override
        public int compareTo(@Nonnull final TenantUser other) {
            return (getName() + "#" + getId()).compareTo(other.getName() + "#" + other.getId());
        }

        @Override
        public String toString() {
            return userId;
        }

        @Override
        public int hashCode() {
            return userId.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof TenantUser && getId().equals(((TenantUser) other).getId());
        }
    }

    @Reference
    protected void setResolverFactory(ResourceResolverFactory factory) {
        resolverFactory = factory;
    }

    @Reference
    protected void setPlatformAccessService(PlatformAccessService service) {
        accessService = service;
    }

    @Reference
    protected void setPermissionsService(PermissionsService service) {
        permissionsService = service;
    }

    @Override
    public boolean isInRole(@Nonnull final String tenantId, @Nonnull final Role role, @Nonnull final String userId) {
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            JackrabbitSession session = (JackrabbitSession) serviceResolver.adaptTo(Session.class);
            if (session != null) {
                final UserManager userManager = session.getUserManager();
                Authorizable member = userManager.getAuthorizable(userId);
                if (member != null) {
                    Authorizable group = userManager.getAuthorizable(getGroupId(tenantId, role));
                    if (group instanceof Group) {
                        return ((Group) group).isMember(member);
                    }
                }
            } else {
                LOG.error("can't adapt to session");
            }
        } catch (LoginException | RepositoryException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    @Nullable
    public TenantUser getTenantUser(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                                    @Nonnull final String userId)
            throws RepositoryException {
        return call((session, context) -> {
            PlatformTenantUser result = null;
            final UserManager userManager = session.getUserManager();
            final Authorizable user = userManager.getAuthorizable(userId);
            if (user instanceof User
                    && (isMember(userManager, tenantId, user, Role.member)
                    || isMember(userManager, tenantId, user, Role.visitor))) {
                result = loadUser(userManager, tenantId, (User) user);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTenantUser({},{}): {}", tenantId, userId, result);
            }
            return result;
        }, resolver, tenantId);
    }

    @Override
    @Nonnull
    public TenantUsers getTenantUsers(@Nonnull final ResourceResolver resolver,
                                      @Nonnull final String tenantId)
            throws RepositoryException {
        return call((session, context) -> {
            List<User> users = new ArrayList<>();
            final UserManager userManager = session.getUserManager();
            for (Role role : TENANT_USER_ROLES) {
                Authorizable group = userManager.getAuthorizable(getGroupId(tenantId, role));
                if (group instanceof Group) {
                    Iterator<Authorizable> members = ((Group) group).getMembers();
                    while (members.hasNext()) {
                        Authorizable user = members.next();
                        if (user instanceof User && !users.contains(user)) {
                            users.add((User) user);
                        }
                    }
                }
            }
            TenantUsers result = new TenantUsers();
            for (User user : users) {
                result.add(loadUser(userManager, tenantId, user));
            }
            Collections.sort(result);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTenantUsers({}): {}", tenantId, users.size());
            }
            return result;
        }, resolver, tenantId);
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

    //

    protected PlatformTenantUser loadUser(@Nonnull final UserManager userManager, @Nonnull final String tenantId,
                                          @Nonnull final User user)
            throws RepositoryException {
        List<Role> roles = new ArrayList<>();
        Iterator<Group> groups = user.memberOf();
        while (groups.hasNext()) {
            Role role = getGroupRole(tenantId, groups.next());
            if (role != null) {
                roles.add(role);
            }
        }
        Value[] lastLogin = user.getProperty(PN_LAST_LOGIN);
        return new PlatformTenantUser(user.getID(), "", "",
                lastLogin != null && lastLogin.length > 0 ? lastLogin[0].getDate() : null,
                roles.toArray(new Role[0]));
    }

    protected boolean isMember(@Nonnull final UserManager userManager, @Nonnull final String tenantId,
                               @Nonnull final Authorizable user, @Nonnull final Role role)
            throws RepositoryException {
        Authorizable group = userManager.getAuthorizable(getGroupId(tenantId, role));
        return group instanceof Group && ((Group) group).isMember(user);
    }

    // group change methods

    protected Role getGroupRole(@Nonnull final String tenantId, @Nonnull final Group group)
            throws RepositoryException {
        String id = group.getID();
        String startPattern = TENANT_GROUP_PREFIX + tenantId + "-";
        if (id.startsWith(startPattern) && id.endsWith("s")) {
            id = id.substring(startPattern.length());
            id = id.substring(0, id.length() - 1);
            try {
                return Role.valueOf(id);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    protected String getGroupId(@Nonnull final String tenantId, @Nonnull final Role role) {
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("using context resolver ({})...", context.getUserID());
                }
                session = (JackrabbitSession) context.adaptTo(Session.class);
                if (session != null) {
                    result = task.call(session, context);
                    session.save();
                } else {
                    throw new RepositoryException("can't adapt to session");
                }
            }
        } else {
            throw new RepositoryException("can't adapt to session");
        }
        return result;
    }
}