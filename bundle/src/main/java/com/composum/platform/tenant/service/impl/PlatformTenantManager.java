package com.composum.platform.tenant.service.impl;

import com.composum.platform.tenant.service.PlatformTenantHook;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.impl.PlatformTenant.Status;
import com.composum.sling.core.filter.ResourceFilter;
import com.composum.sling.core.filter.StringFilter;
import com.composum.sling.core.service.PermissionsService;
import com.composum.sling.core.util.ResourceUtil;
import com.composum.sling.platform.security.PlatformAccessService;
import com.composum.sling.platform.security.PlatformAccessService.AccessContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.tenant.Tenant;
import org.apache.sling.tenant.TenantManager;
import org.apache.sling.tenant.TenantProvider;
import org.apache.sling.tenant.spi.TenantManagerHook;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.composum.platform.tenant.service.impl.PlatformTenant.CPM_ACTIVATED;
import static com.composum.platform.tenant.service.impl.PlatformTenant.CPM_CREATED;
import static com.composum.platform.tenant.service.impl.PlatformTenant.CPM_DEACTIVATED;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_APPLICATION_ROOT;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_CONTENT_ROOT;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_PRINCIPAL_BASE;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_STATUS;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Manager"
        },
        immediate = true,
        service = {TenantManagerService.class, TenantManager.class, TenantProvider.class}
)
@Designate(ocd = PlatformTenantManager.Configuration.class)
public class PlatformTenantManager implements TenantManagerService, TenantManager, TenantProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTenantManager.class);

    @ObjectClassDefinition(
            name = "Composum Platform Tenant Configuration"
    )
    @interface Configuration {

        @AttributeDefinition(
                name = "Tenant Configuration Root",
                description = "the tenants configuration root path; default: '/etc/tenants'"
        )
        String tenant_root() default "/etc/tenants";

        @AttributeDefinition(
                name = "Tenant Path Matcher",
                description = "Defines tenants path matcher to resolve a tenant (assuming group '1' is the tenant id)"
        )
        String[] tenant_path_matcher() default {
                "^/content/([^/]+)(/.*)?",
                "^/preview/([^/]+)(/.*)?",
                "^/public/([^/]+)(/.*)?",
                "^/apps/([^/]+)(/.*)?",
                "^/etc/tenants/([^/]+)(/.*)?"
        };

        @AttributeDefinition(
                name = "Tenant Content Root",
                description = "the tenants content root path; default: '/content'"
        )
        String tenant_content_root() default "/content";

        @AttributeDefinition(
                name = "Tenant Application Root",
                description = "the tenants content root path; default: '/apps'"
        )
        String tenant_application_root() default "/apps";

        @AttributeDefinition(
                name = "Tenant Principal Base",
                description = "the tenants principal base (relative path); default: 'tenants'"
        )
        String tenant_principal_base() default "tenants";

        @AttributeDefinition(
                name = "Tenant Node Primary Type",
                description = "the primary type of a new tenant node instance; default: 'sling:Folder'"
        )
        String tenant_primary_type() default "sling:Folder";

        @AttributeDefinition(
                name = "Tenant Resource Type",
                description = "the Sling resource type of a new tenant resource; default: 'composum/platform/tenant/component'"
        )
        String tenant_resource_type() default "composum/platform/tenant/component";

        @AttributeDefinition(
                name = "Tenant ID pattern",
                description = "the pattern to check an id for a new tenant; default: '^[a-zA-Z_][a-zA-Z_0-9-]+$'"
        )
        String tenant_id_pattern() default "^[a-zA-Z_][a-zA-Z_0-9-]+$";

        @AttributeDefinition(
                name = "Reserved Tenant IDs",
                description = "a list of reserved id patterns (not usable for a tenant)"
        )
        String[] tenant_id_reserved() default {"^.*composum.*$", "^shared.*$", "^site.*$", "^.*sling.*$"};

        @AttributeDefinition(
                name = "Immutable Tenant Properties",
                description = "a list of property names which are immutable"
        )
        String[] tenant_props_immutable() default {
                JcrConstants.JCR_PRIMARYTYPE,
                JcrConstants.JCR_MIXINTYPES,
                PN_STATUS,
                PN_CONTENT_ROOT,
                PN_APPLICATION_ROOT,
                PN_PRINCIPAL_BASE
        };

        @AttributeDefinition(
                name = "Protected Tenant Properties",
                description = "a list of property names which are protected (for administrators only)"
        )
        String[] tenant_props_protected() default {
                JcrConstants.JCR_PRIMARYTYPE,
                JcrConstants.JCR_MIXINTYPES,
                JcrConstants.JCR_CREATED,
                JcrConstants.JCR_CREATED + "By",
                JcrConstants.JCR_LASTMODIFIED,
                JcrConstants.JCR_LASTMODIFIED + "By",
                ResourceUtil.PROP_RESOURCE_TYPE,
                CPM_CREATED + "By",
                CPM_DEACTIVATED,
                CPM_DEACTIVATED + "By",
                CPM_ACTIVATED,
                CPM_ACTIVATED + "By",
                PN_PRINCIPAL_BASE,
                PN_STATUS
        };

        @AttributeDefinition(
                name = "Allowed Platform Hooks",
                description = "a list of package names which are designated to register platform hooks"
        )
        String[] tenant_hooks_allowed() default {
                "com.composum.platform.tenant"
        };
    }

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private PlatformAccessService accessService;

    @Reference
    private PermissionsService permissionsService;

    protected List<PlatformTenantHook> platformHooks = Collections.synchronizedList(new ArrayList<PlatformTenantHook>());
    protected List<TenantManagerHook> managerHooks = Collections.synchronizedList(new ArrayList<TenantManagerHook>());

    protected PlatformTenantAdapter adapterFactory;
    protected ServiceRegistration<?> adapterFactoryService;

    protected Configuration config;

    @Activate
    @Modified
    protected void activate(BundleContext bundleContext, Configuration config) {
        this.config = config;
        adapterFactory = new PlatformTenantAdapter(this, config.tenant_path_matcher());
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(Constants.SERVICE_DESCRIPTION, "Composum Platform Tenant Adapter");
        props.put(AdapterFactory.ADAPTER_CLASSES, new String[]{Tenant.class.getName(), PlatformTenant.class.getName()});
        props.put(AdapterFactory.ADAPTABLE_CLASSES, new String[]{Resource.class.getName()});
        adapterFactoryService = bundleContext.registerService(AdapterFactory.SERVICE_NAME, adapterFactory, props);
    }

    @Deactivate
    protected void deactivate() {
        if (adapterFactoryService != null) {
            adapterFactoryService.unregister();
            adapterFactoryService = null;
        }
    }

    @Reference(
            service = TenantManagerHook.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    protected void bindTenantManagerHook(@Nonnull final TenantManagerHook service) {
        LOG.info("bindTenantHook: {}", service.getClass());
        managerHooks.add(service);
    }

    protected void unbindTenantManagerHook(@Nonnull final TenantManagerHook service) {
        LOG.info("unbindTenantHook: {}", service);
        managerHooks.remove(service);
    }

    @Reference(
            service = PlatformTenantHook.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    protected void bindPlatformTenantHook(@Nonnull final PlatformTenantHook service) {
        String pckgName = service.getClass().getPackage().getName();
        for (String pckgPattern : config.tenant_hooks_allowed()) {
            if (pckgName.startsWith(pckgPattern)) {
                LOG.info("bindPlatformHook: {}", service.getClass());
                platformHooks.add(service);
                return;
            }
        }
        LOG.error("platform hook '{}' not allowed; check configuration", service.getClass().getName());
    }

    protected void unbindPlatformTenantHook(@Nonnull final PlatformTenantHook service) {
        LOG.info("unbindPlatformHook: {}", service);
        platformHooks.remove(service);
    }

    // permission check

    protected final RetrievalGranted retrievalGranted = new RetrievalGranted();
    protected final ChangingGranted changingGranted = new ChangingGranted();
    protected final ManagingGranted managingGranted = new ManagingGranted();

    private final class RetrievalGranted implements PermissionCheck {
        @Override
        public boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId) {
            try {
                Session session = resolver.adaptTo(Session.class);
                String tenantsRootPath = getTenantsRoot(resolver).getPath();
                return permissionsService.hasAllPrivileges(session,
                        StringUtils.isBlank(tenantId) ? tenantsRootPath : tenantsRootPath + "/" + tenantId,
                        "jcr:read");
            } catch (Exception ignore) {
                return false;
            }
        }
    }

    private final class ChangingGranted implements PermissionCheck {
        @Override
        public boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId) {
            try {
                Session session = resolver.adaptTo(Session.class);
                String tenantsRootPath = getTenantsRoot(resolver).getPath();
                return StringUtils.isNotBlank(tenantId)
                        && permissionsService.isMemberOfOne(session,
                        "administrators",
                        "composum-platform-administrators",
                        "tenant-" + tenantId + "-managers") != null
                        && permissionsService.hasAllPrivileges(session, tenantsRootPath + "/" + tenantId,
                        "jcr:read");
            } catch (Exception ignore) {
                return false;
            }
        }
    }

    private final class ManagingGranted implements PermissionCheck {
        @Override
        public boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId) {
            try {
                Session session = resolver.adaptTo(Session.class);
                String tenantsRootPath = getTenantsRoot(resolver).getPath();
                return permissionsService.isMemberOfOne(session,
                        "administrators",
                        "composum-platform-administrators") != null
                        && permissionsService.hasAllPrivileges(session, tenantsRootPath,
                        "rep:write");
            } catch (Exception ignore) {
                return false;
            }
        }
    }

    // TenantManagerService

    @Override
    @Nonnull
    public final Resource getTenantsRoot(@Nonnull ResourceResolver resolver) {
        Resource tenantsRoot = resolver.getResource(config.tenant_root());
        if (tenantsRoot == null) {
            throw new IllegalStateException("tenants root not available");
        }
        return tenantsRoot;
    }

    protected final PlatformTenant toTenant(@Nonnull final ResourceResolver context,
                                            @Nullable final Resource tenantResource,
                                            boolean checkPermission) {
        if (tenantResource != null) {
            ValueMap values = tenantResource.getValueMap();
            Status status = Status.valueOf(values.get(PN_STATUS, Status.active.name()));
            Map<String, Object> properties = new HashMap<>();
            List<String> protectd = managingGranted.isAccessGranted(context, tenantResource.getName())
                    ? Collections.<String>emptyList() : Arrays.asList(config.tenant_props_protected());
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                if (!protectd.contains(key)) {
                    properties.put(key, entry.getValue());
                }
            }
            return new PlatformTenant(tenantResource.getName(), status, new ValueMapDecorator(properties));
        }
        return null;
    }

    @Override
    @Nullable
    public final PlatformTenant getTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId) {
        return call(new ResourceResolverTask<PlatformTenant>() {
            @Override
            public PlatformTenant call(@Nonnull final ResourceResolver resolver,
                                       @Nonnull final ResourceResolver context) {
                final Resource tenantsRoot = getTenantsRoot(resolver);
                return toTenant(context, tenantsRoot.getChild(tenantId), true);
            }
        }, resolver);
    }

    protected class TenantList extends ArrayList<Tenant> {
        protected TenantList(@Nonnull final ResourceResolver context,
                             @Nonnull final Iterator<Resource> resourceIterator,
                             @Nonnull final ResourceFilter tenantFilter) {
            while (resourceIterator.hasNext()) {
                Resource resource = resourceIterator.next();
                if (tenantFilter.accept(resource)) {
                    add(toTenant(context, resource, true));
                }
            }
        }
    }

    @Override
    @Nonnull
    public Iterator<Tenant> getTenants(@Nonnull final ResourceResolver resolver,
                                       @Nullable final ResourceFilter filter) {
        Iterator<Tenant> result = retrieve(new ResourceResolverTask<Iterator<Tenant>>() {
            @Override
            public final Iterator<Tenant> call(@Nonnull final ResourceResolver resolver,
                                               @Nonnull final ResourceResolver context) {
                // use context resolver (request) to avoid access cross tenant without access rights
                final Resource tenantsRoot = getTenantsRoot(context);
                ResourceFilter resourceFilter = new ResourceFilter.PrimaryTypeFilter(
                        new StringFilter.WhiteList(config.tenant_primary_type()));
                if (filter != null) {
                    resourceFilter = new ResourceFilter.FilterSet(
                            ResourceFilter.FilterSet.Rule.and, resourceFilter, filter);
                }
                return new TenantList(context, tenantsRoot.listChildren(), resourceFilter).iterator();
            }
        }, resolver, null);
        return result != null ? result : Collections.<Tenant>emptyIterator();
    }

    @Override
    @Nonnull
    public final PlatformTenant createTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                                             @Nullable final Map<String, Object> properties)
            throws PersistenceException {
        return manage(new ResourceResolverTask<PlatformTenant>() {
            @Override
            public final PlatformTenant call(@Nonnull final ResourceResolver resolver,
                                             @Nonnull final ResourceResolver context)
                    throws PersistenceException {
                if (StringUtils.isBlank(tenantId) || !tenantId.matches(config.tenant_id_pattern())) {
                    throw new PersistenceException("tenant id is not valid");
                }
                for (String pattern : config.tenant_id_reserved()) {
                    if (Pattern.compile(pattern).matcher(tenantId).matches()) {
                        throw new PersistenceException("tenant id is reserved");
                    }
                }
                final Resource tenantsRoot = getTenantsRoot(resolver);
                if (tenantsRoot.getChild(tenantId) != null) {
                    throw new PersistenceException("tenant id is already in use");
                }
                String value;
                final Map<String, Object> initialProps = new HashMap<>();
                if (StringUtils.isNotBlank(value = config.tenant_resource_type())) {
                    initialProps.put(ResourceUtil.PROP_RESOURCE_TYPE, value);
                }
                if (properties != null) {
                    initialProps.putAll(properties);
                }
                initialProps.put(JcrConstants.JCR_PRIMARYTYPE, config.tenant_primary_type());
                initialProps.put(PN_CONTENT_ROOT, config.tenant_content_root() + "/" + tenantId);
                initialProps.put(PN_APPLICATION_ROOT, config.tenant_application_root() + "/" + tenantId);
                initialProps.put(PN_PRINCIPAL_BASE, config.tenant_principal_base() + "/" + tenantId);
                Resource tenantResource = resolver.create(tenantsRoot, tenantId, initialProps);
                PlatformTenant tenant = toTenant(resolver, tenantResource, false);
                final ModifiableValueMap tenantProps = getProperties(resolver, tenantResource);
                tenantProps.put(CPM_CREATED + "By", context.getUserID());
                for (TenantManagerHook hook : managerHooks) {
                    Map<String, Object> changes = hook.setup(tenant);
                    if (changes != null && changes.size() > 0) {
                        updateTenant(tenantProps, changes);
                    }
                }
                tenant = toTenant(resolver, tenantResource, false);
                for (PlatformTenantHook hook : platformHooks) {
                    Map<String, Object> changes = hook.setup(resolver, context, tenant);
                    if (changes != null && changes.size() > 0) {
                        updateTenant(tenantProps, changes);
                    }
                }
                LOG.info("createTenant({}): {}", tenantId, tenant);
                return toTenant(resolver, tenantResource, false);
            }
        }, resolver, tenantId);
    }

    @Override
    public final void changeTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                                   @Nonnull final Map<String, Object> properties)
            throws PersistenceException {
        change(new ResourceResolverTask<Void>() {
            @Override
            public final Void call(@Nonnull final ResourceResolver resolver,
                                   @Nonnull final ResourceResolver context)
                    throws PersistenceException {
                final Resource tenantsRoot = getTenantsRoot(resolver);
                final Resource tenantResource = tenantsRoot.getChild(tenantId);
                if (tenantResource != null) {
                    PlatformTenant tenant = toTenant(resolver, tenantResource, true);
                    LOG.info("changeTenant({})", tenant);
                    final ModifiableValueMap tenantProps = getProperties(resolver, tenantResource);
                    setTimestamp(tenantProps, context, JcrConstants.JCR_LASTMODIFIED);
                    updateTenant(tenantProps, properties);
                    for (TenantManagerHook hook : managerHooks) {
                        Map<String, Object> changes = hook.change(tenant);
                        if (changes != null && changes.size() > 0) {
                            updateTenant(tenantProps, changes);
                        }
                    }
                    tenant = toTenant(resolver, tenantResource, true);
                    for (PlatformTenantHook hook : platformHooks) {
                        Map<String, Object> changes = hook.change(resolver, context, tenant);
                        if (changes != null && changes.size() > 0) {
                            updateTenant(tenantProps, changes);
                        }
                    }
                } else {
                    throw new PersistenceException("tenant '" + tenantId + "' not found");
                }
                return null;
            }
        }, resolver, tenantId);
    }

    private void updateTenant(@Nonnull final ModifiableValueMap tenantProps,
                              @Nonnull final Map<String, Object> properties) {
        List<String> immutable = Arrays.asList(config.tenant_props_immutable());
        for (String key : properties.keySet()) {
            if (!immutable.contains(key)) {
                Object value = properties.get(key);
                if (value != null) {
                    tenantProps.put(key, value);
                } else {
                    tenantProps.remove(key);
                }
            }
        }
    }

    @Override
    public final void deactivateTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId)
            throws PersistenceException {
        manage(new ResourceResolverTask<Void>() {
            @Override
            public final Void call(@Nonnull final ResourceResolver resolver,
                                   @Nonnull final ResourceResolver context)
                    throws PersistenceException {
                final Resource tenantsRoot = getTenantsRoot(resolver);
                final Resource tenantResource = tenantsRoot.getChild(tenantId);
                PlatformTenant tenant = toTenant(context, tenantResource, true);
                if (tenant != null && tenant.getStatus() != Status.deactivated) {
                    LOG.info("deactivateTenant({})", tenant);
                    doDeactivate(resolver, context, tenantResource);
                } else {
                    throw new PersistenceException("tenant '" + tenantId + "' not found");
                }
                return null;
            }
        }, resolver, tenantId);
    }

    @Override
    public final void reanimateTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId)
            throws PersistenceException {
        manage(new ResourceResolverTask<Void>() {
            @Override
            public final Void call(@Nonnull final ResourceResolver resolver,
                                   @Nonnull final ResourceResolver context)
                    throws PersistenceException {
                final Resource tenantsRoot = getTenantsRoot(resolver);
                final Resource tenantResource = tenantsRoot.getChild(tenantId);
                PlatformTenant tenant = toTenant(context, tenantResource, true);
                if (tenant != null && tenant.getStatus() == Status.deactivated) {
                    LOG.info("activateTenant({})", tenant);
                    doActivate(resolver, context, tenantResource);
                } else {
                    throw new PersistenceException("tenant '" + tenantId + "' not found");
                }
                return null;
            }
        }, resolver, tenantId);
    }


    @Override
    public final void deleteTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId)
            throws PersistenceException {
        manage(new ResourceResolverTask<Void>() {
            @Override
            public final Void call(@Nonnull final ResourceResolver resolver,
                                   @Nonnull final ResourceResolver context)
                    throws PersistenceException {
                final Resource tenantsRoot = getTenantsRoot(resolver);
                final Resource tenantResource = tenantsRoot.getChild(tenantId);
                PlatformTenant tenant = toTenant(context, tenantResource, false);
                if (tenant != null) {
                    if (tenant.getStatus() == Status.active) {
                        LOG.info("delete->deactivateTenant({})", tenant);
                        doDeactivate(resolver, context, tenantResource);
                    } else {
                        LOG.info("deleteTenant({})", tenant);
                        for (PlatformTenantHook hook : platformHooks) {
                            hook.remove(resolver, context, tenant);
                        }
                        for (TenantManagerHook hook : managerHooks) {
                            hook.remove(tenant);
                        }
                        resolver.delete(tenantResource);
                    }
                } else {
                    throw new PersistenceException("tenant '" + tenantId + "' not found");
                }
                return null;
            }
        }, resolver, tenantId);
    }

    //

    private Calendar now() {
        Calendar now = new GregorianCalendar();
        now.setTime(new Date());
        return now;
    }

    private ModifiableValueMap getProperties(@Nonnull final ResourceResolver resolver,
                                             @Nonnull final Resource tenantResource)
            throws PersistenceException {
        final ModifiableValueMap tenantProps = tenantResource.adaptTo(ModifiableValueMap.class);
        if (tenantProps == null) {
            throw new PersistenceException("tenant '" + tenantResource.getName() + "' is immutable");
        }
        return tenantProps;
    }

    private ModifiableValueMap setTimestamp(@Nonnull final ModifiableValueMap tenantProps,
                                            @Nonnull final ResourceResolver context,
                                            @Nonnull final String timestampProperty) {
        tenantProps.put(timestampProperty, now());
        tenantProps.put(timestampProperty + "By", context.getUserID());
        return tenantProps;
    }

    private void changeStatus(@Nonnull final ResourceResolver resolver,
                              @Nonnull final ResourceResolver context,
                              @Nonnull final Resource tenantResource,
                              @Nonnull final Status status, @Nonnull final String timestampProperty)
            throws PersistenceException {
        final ModifiableValueMap tenantProps = getProperties(resolver, tenantResource);
        tenantProps.put(PN_STATUS, status.name());
        setTimestamp(tenantProps, context, timestampProperty);
        PlatformTenant tenant = toTenant(context, tenantResource, false);
        for (PlatformTenantHook hook : platformHooks) {
            hook.change(resolver, context, tenant);
        }
    }

    private void doDeactivate(@Nonnull final ResourceResolver resolver, @Nonnull final ResourceResolver context,
                              @Nonnull final Resource tenantResource)
            throws PersistenceException {
        changeStatus(resolver, context, tenantResource, Status.deactivated, CPM_DEACTIVATED);
    }

    private void doActivate(@Nonnull final ResourceResolver resolver, @Nonnull final ResourceResolver context,
                            @Nonnull final Resource tenantResource)
            throws PersistenceException {
        changeStatus(resolver, context, tenantResource, Status.active, CPM_ACTIVATED);
    }

    // TenantManager

    @Override
    public final Tenant create(final String tenantId, final Map<String, Object> properties) {
        try {
            return manage(new ResourceResolverTask<Tenant>() {
                @Override
                public Tenant call(@Nonnull final ResourceResolver resolver,
                                   @Nonnull final ResourceResolver context)
                        throws PersistenceException {
                    return createTenant(resolver, tenantId, properties);
                }
            }, null, null);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public final void remove(@Nonnull final Tenant tenant) {
        try {
            manage(new ResourceResolverTask<Void>() {
                @Override
                public Void call(@Nonnull final ResourceResolver resolver,
                                 @Nonnull final ResourceResolver context)
                        throws PersistenceException {
                    deleteTenant(resolver, tenant.getId());
                    return null;
                }
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public final void setProperty(@Nonnull final Tenant tenant,
                                  @Nonnull final String name, @Nullable final Object value) {
        try {
            change(new ResourceResolverTask<Void>() {
                @Override
                public Void call(@Nonnull final ResourceResolver resolver,
                                 @Nonnull final ResourceResolver context)
                        throws PersistenceException {
                    changeTenant(resolver, tenant.getId(), new HashMap<String, Object>() {{
                        put(name, value);
                    }});
                    return null;
                }
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public final void setProperties(@Nonnull final Tenant tenant,
                                    @Nonnull final Map<String, Object> properties) {
        try {
            change(new ResourceResolverTask<Void>() {
                @Override
                public Void call(@Nonnull final ResourceResolver resolver,
                                 @Nonnull final ResourceResolver context)
                        throws PersistenceException {
                    changeTenant(resolver, tenant.getId(), properties);
                    return null;
                }
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public final void removeProperties(@Nonnull final Tenant tenant, final String... propertyNames) {
        try {
            change(new ResourceResolverTask<Void>() {
                @Override
                public Void call(@Nonnull final ResourceResolver resolver,
                                 @Nonnull final ResourceResolver context)
                        throws PersistenceException {
                    Map<String, Object> properties = new HashMap<>();
                    for (String key : propertyNames) {
                        properties.put(key, null);
                    }
                    changeTenant(resolver, tenant.getId(), properties);
                    return null;
                }
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    // TenantProvider

    @Override
    public final Tenant getTenant(@Nonnull final String tenantId) {
        return call(new ResourceResolverTask<Tenant>() {
            @Override
            public Tenant call(@Nonnull final ResourceResolver resolver,
                               @Nonnull final ResourceResolver context) {
                return getTenant(resolver, tenantId);
            }
        }, null);
    }

    @Override
    public final Iterator<Tenant> getTenants() {
        return getTenants(null);
    }

    @Override
    public final Iterator<Tenant> getTenants(@Nullable final String tenantFilter) {
        return retrieve(new ResourceResolverTask<Iterator<Tenant>>() {
            @Override
            public Iterator<Tenant> call(@Nonnull final ResourceResolver resolver,
                                         @Nonnull final ResourceResolver context) {
                ResourceFilter resourceFilter = null;
                if (StringUtils.isNotBlank(tenantFilter)) {
                    try {
                        final Filter osgiFilter = FrameworkUtil.createFilter(tenantFilter);
                        resourceFilter = new ResourceFilter() {

                            @Override
                            public boolean accept(Resource resource) {
                                return osgiFilter.matches(resource.getValueMap());
                            }

                            @Override
                            public boolean isRestriction() {
                                return true;
                            }

                            @Override
                            public void toString(StringBuilder builder) {
                                builder.append(osgiFilter.toString());
                            }
                        };
                    } catch (InvalidSyntaxException ex) {
                        LOG.error(ex.toString());
                    }
                }
                return getTenants(resolver, resourceFilter);
            }
        }, null, null);
    }

    //
    // to implement the interface methods driven in various resolver contexts
    //

    private interface PermissionCheck {

        /**
         * returns 'true' if requested access is granted
         *
         * @param resolver the resolver to use (context resolver) fpr permission check
         */
        boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId);
    }

    private interface ResourceResolverTask<T> {

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

    /**
     * call using service resolver (without permission check!)...
     */
    private <T> T call(@Nonnull final ResourceResolverTask<T> task, @Nullable ResourceResolver context) {
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
     * call action using service resolver if retrieval access is granted otherwise the given resolver...
     */
    private <T> T retrieve(@Nonnull final ResourceResolverTask<T> task,
                           @Nullable final ResourceResolver context, @Nullable String tenantId) {
        try {
            return call(task, retrievalGranted, context, tenantId);
        } catch (IllegalStateException ignore) {
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * call action using service resolver if changing access is granted otherwise the given resolver...
     */
    private <T> T change(@Nonnull final ResourceResolverTask<T> task,
                         @Nullable final ResourceResolver context, @Nullable String tenantId)
            throws PersistenceException {
        return call(task, changingGranted, context, tenantId);
    }

    /**
     * call action using service resolver if managing access is granted otherwise the given resolver...
     */
    private <T> T manage(@Nonnull final ResourceResolverTask<T> task,
                         @Nullable final ResourceResolver context, @Nullable String tenantId)
            throws PersistenceException {
        return call(task, managingGranted, context, tenantId);
    }

    /**
     * call action using service resolver if requested access is granted otherwise the given resolver...
     */
    private <T> T call(@Nonnull final ResourceResolverTask<T> task, @Nullable PermissionCheck permissionCheck,
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

    private ResourceResolver getAccessContextResolver() throws IllegalAccessException {
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
