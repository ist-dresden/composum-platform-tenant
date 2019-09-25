package com.composum.platform.tenant.service.impl;

import com.composum.platform.tenant.service.PlatformTenantHook;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.impl.PlatformTenant.Status;
import com.composum.sling.core.filter.ResourceFilter;
import com.composum.sling.core.filter.StringFilter;
import com.composum.sling.core.service.PermissionsService;
import com.composum.sling.core.util.ResourceUtil;
import com.composum.sling.platform.security.PlatformAccessService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.adapter.AdapterFactory;
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
import java.util.Objects;
import java.util.regex.Pattern;

import static com.composum.platform.tenant.service.impl.PlatformTenant.CPM_ACTIVATED;
import static com.composum.platform.tenant.service.impl.PlatformTenant.CPM_CREATED;
import static com.composum.platform.tenant.service.impl.PlatformTenant.CPM_DEACTIVATED;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_APPLICATION_ROOT;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_CONTENT_ROOT;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_PREVIEW_ROOT;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_PRINCIPAL_BASE;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_PUBLIC_ROOT;
import static com.composum.platform.tenant.service.impl.PlatformTenant.PN_STATUS;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Manager"
        },
        immediate = true,
        service = {TenantManagerService.class, TenantManager.class, TenantProvider.class}
)
@Designate(ocd = PlatformTenantManager.Configuration.class)
public final class PlatformTenantManager extends AbstractTenantService
        implements TenantManagerService, TenantManager, TenantProvider {

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
                "^/etc/tenants/([^/]+)(/.*)?",
                "^/var/composum/workflow/([^/]+)(/.*)?"
        };

        @AttributeDefinition(
                name = "Tenant Public Root",
                description = "the tenants public stage root path; default: '/public'"
        )
        String tenant_public_root() default "/public";

        @AttributeDefinition(
                name = "Tenant Preview Root",
                description = "the tenants preview stage root path; default: '/preview'"
        )
        String tenant_preview_root() default "/preview";

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
                name = "Tenant ID pattern",
                description = "the pattern to check an id for a new tenant; default: '^[a-zA-Z_][a-zA-Z_0-9]+$'"
        )
        String tenant_id_pattern() default "^[a-zA-Z_][a-zA-Z_0-9]+$";

        @AttributeDefinition(
                name = "Reserved Tenant IDs",
                description = "a list of reserved id patterns (not usable for a tenant)"
        )
        String[] tenant_id_reserved() default {"^composum.*$", "^platform$", "^general$", "^shared$", "^sling.*$"};

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

    protected static final ResourceFilter TENANT_NODE_FILTER =
            new ResourceFilter.ResourceTypeFilter(new StringFilter.WhiteList(TENANT_RESOURCE_TYPE));

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

    protected List<PlatformTenantHook> earlyHooks = Collections.synchronizedList(new ArrayList<>());
    protected List<PlatformTenantHook> platformHooks = Collections.synchronizedList(new ArrayList<>());
    protected List<TenantManagerHook> managerHooks = Collections.synchronizedList(new ArrayList<>());

    protected PlatformTenantAdapter adapterFactory;
    protected ServiceRegistration<?> adapterFactoryService;

    protected Configuration config;

    @Activate
    @Modified
    protected void activate(BundleContext bundleContext, Configuration config) {
        this.config = config;
        for (PlatformTenantHook hook : earlyHooks) {
            bindAllowedTenantHook(hook);
        }
        earlyHooks.clear();
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
        if (config != null) {
            bindAllowedTenantHook(service);
        } else {
            earlyHooks.add(service);
        }
    }

    protected void bindAllowedTenantHook(@Nonnull final PlatformTenantHook service) {
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
        PlatformTenant tenant = null;
        if (tenantResource != null && tenantResource.isResourceType(TENANT_RESOURCE_TYPE)) {
            ValueMap values = tenantResource.getValueMap();
            Status status = Status.valueOf(values.get(PN_STATUS, Status.active.name()));
            Map<String, Object> properties = new HashMap<>();
            List<String> protectd = managingGranted.isAccessGranted(context, tenantResource.getName())
                    ? Collections.emptyList() : Arrays.asList(config.tenant_props_protected());
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                if (!protectd.contains(key)) {
                    properties.put(key, entry.getValue());
                }
            }
            tenant = new PlatformTenant(tenantResource.getName(), status, new ValueMapDecorator(properties));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("toTenant({}): {}", tenantResource != null ? tenantResource.getPath() : "NULL", tenant);
        }
        return tenant;
    }

    @Override
    @Nullable
    public final PlatformTenant getTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId) {
        return call((resolver1, context) -> {
            final Resource tenantsRoot = getTenantsRoot(resolver1);
            return toTenant(context, tenantsRoot.getChild(tenantId), true);
        }, resolver);
    }

    protected class TenantList extends ArrayList<Tenant> {
        protected TenantList(@Nonnull final ResourceResolver context,
                             @Nonnull final Iterator<Resource> resourceIterator,
                             @Nonnull final ResourceFilter tenantFilter) {
            while (resourceIterator.hasNext()) {
                Resource resource = resourceIterator.next();
                if (tenantFilter.accept(resource)) {
                    Tenant tenant = toTenant(context, resource, true);
                    if (tenant != null) {
                        add(tenant);
                    }
                }
            }
        }
    }

    @Override
    @Nonnull
    public Iterator<Tenant> getTenants(@Nonnull final ResourceResolver resolver,
                                       @Nullable final ResourceFilter filter) {
        Iterator<Tenant> result = retrieve((resolver1, context) -> {
            // use context resolver (request) to avoid access cross tenant without access rights
            final Resource tenantsRoot = getTenantsRoot(context);
            ResourceFilter resourceFilter = TENANT_NODE_FILTER;
            if (filter != null) {
                resourceFilter = new ResourceFilter.FilterSet(
                        ResourceFilter.FilterSet.Rule.and, resourceFilter, filter);
            }
            TenantList tenants = new TenantList(context, tenantsRoot.listChildren(), resourceFilter);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTenants({}): {}", resolver.getUserID(), tenants.size());
            }
            return tenants.iterator();
        }, resolver, null);
        return result != null ? result : Collections.emptyIterator();
    }

    @Override
    @Nullable
    public PersistenceException isTenantAllowed(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                                                @Nullable final Map<String, Object> properties) {
        return call((resolver1, context) -> {
            if (StringUtils.isBlank(tenantId) || !tenantId.matches(config.tenant_id_pattern())) {
                return new PersistenceException("tenant id is not valid");
            }
            if (!"admin".equals(resolver.getUserID())) {
                for (String pattern : config.tenant_id_reserved()) {
                    if (Pattern.compile(pattern).matcher(tenantId).matches()) {
                        return new PersistenceException("tenant id is reserved");
                    }
                }
            }
            final Resource tenantsRoot = getTenantsRoot(resolver1);
            if (tenantsRoot.getChild(tenantId) != null) {
                return new PersistenceException("tenant id is in use already");
            }
            return null;
        }, null);
    }

    @Override
    @Nonnull
    public final PlatformTenant createTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                                             @Nullable final Map<String, Object> properties)
            throws PersistenceException {
        return manage((serviceResolver, context) -> {
            PersistenceException checkException = isTenantAllowed(context, tenantId, properties);
            if (checkException != null) {
                throw checkException;
            }
            final Resource tenantsRoot = getTenantsRoot(serviceResolver);
            String value;
            final Map<String, Object> initialProps = new HashMap<>();
            initialProps.put(ResourceUtil.PROP_RESOURCE_TYPE, TENANT_RESOURCE_TYPE);
            if (properties != null) {
                initialProps.putAll(properties);
            }
            initialProps.put(JcrConstants.JCR_PRIMARYTYPE, config.tenant_primary_type());
            initialProps.put(PN_PUBLIC_ROOT, config.tenant_public_root() + "/" + tenantId);
            initialProps.put(PN_PREVIEW_ROOT, config.tenant_preview_root() + "/" + tenantId);
            initialProps.put(PN_CONTENT_ROOT, config.tenant_content_root() + "/" + tenantId);
            initialProps.put(PN_APPLICATION_ROOT, config.tenant_application_root() + "/" + tenantId);
            initialProps.put(PN_PRINCIPAL_BASE, config.tenant_principal_base() + "/" + tenantId);
            Resource tenantResource = serviceResolver.create(tenantsRoot, tenantId, initialProps);
            PlatformTenant tenant = toTenant(serviceResolver, tenantResource, false);
            final ModifiableValueMap tenantProps = getProperties(serviceResolver, tenantResource);
            tenantProps.put(CPM_CREATED + "By", context.getUserID());
            for (TenantManagerHook hook : managerHooks) {
                Map<String, Object> changes = hook.setup(tenant);
                if (changes != null && changes.size() > 0) {
                    updateTenant(tenantProps, changes);
                }
            }
            tenant = toTenant(serviceResolver, tenantResource, false);
            for (PlatformTenantHook hook : platformHooks) {
                Map<String, Object> changes = hook.setup(serviceResolver, context, tenant);
                if (changes != null && changes.size() > 0) {
                    updateTenant(tenantProps, changes);
                }
            }
            LOG.info("createTenant({}): {}", tenantId, tenant);
            return toTenant(serviceResolver, tenantResource, false);
        }, resolver, tenantId);
    }

    @Override
    public final void changeTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                                   @Nonnull final Map<String, Object> properties)
            throws PersistenceException {
        change((ResourceResolverTask<Void>) (resolver1, context) -> {
            final Resource tenantsRoot = getTenantsRoot(resolver1);
            final Resource tenantResource = tenantsRoot.getChild(tenantId);
            if (tenantResource != null) {
                PlatformTenant tenant = toTenant(resolver1, tenantResource, true);
                LOG.info("changeTenant({})", tenant);
                final ModifiableValueMap tenantProps = getProperties(resolver1, tenantResource);
                setTimestamp(tenantProps, context, JcrConstants.JCR_LASTMODIFIED);
                updateTenant(tenantProps, properties);
                for (TenantManagerHook hook : managerHooks) {
                    Map<String, Object> changes = hook.change(tenant);
                    if (changes != null && changes.size() > 0) {
                        updateTenant(tenantProps, changes);
                    }
                }
                tenant = toTenant(resolver1, tenantResource, true);
                for (PlatformTenantHook hook : platformHooks) {
                    Map<String, Object> changes = hook.change(resolver1, context, tenant);
                    if (changes != null && changes.size() > 0) {
                        updateTenant(tenantProps, changes);
                    }
                }
            } else {
                throw new PersistenceException("tenant '" + tenantId + "' not found");
            }
            return null;
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
        manage((ResourceResolverTask<Void>) (resolver1, context) -> {
            final Resource tenantsRoot = getTenantsRoot(resolver1);
            final Resource tenantResource = Objects.requireNonNull(tenantsRoot.getChild(tenantId));
            PlatformTenant tenant = toTenant(context, tenantResource, true);
            if (tenant != null && tenant.getStatus() != Status.deactivated) {
                LOG.info("deactivateTenant({})", tenant);
                doDeactivate(resolver1, context, tenantResource);
            } else {
                throw new PersistenceException("tenant '" + tenantId + "' not found");
            }
            return null;
        }, resolver, tenantId);
    }

    @Override
    public final void reanimateTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId)
            throws PersistenceException {
        manage((ResourceResolverTask<Void>) (resolver1, context) -> {
            final Resource tenantsRoot = getTenantsRoot(resolver1);
            final Resource tenantResource = Objects.requireNonNull(tenantsRoot.getChild(tenantId));
            PlatformTenant tenant = toTenant(context, tenantResource, true);
            if (tenant != null && tenant.getStatus() == Status.deactivated) {
                LOG.info("activateTenant({})", tenant);
                doActivate(resolver1, context, tenantResource);
            } else {
                throw new PersistenceException("tenant '" + tenantId + "' not found");
            }
            return null;
        }, resolver, tenantId);
    }


    @Override
    public final void deleteTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId)
            throws PersistenceException {
        manage((ResourceResolverTask<Void>) (resolver1, context) -> {
            final Resource tenantsRoot = getTenantsRoot(resolver1);
            final Resource tenantResource = Objects.requireNonNull(tenantsRoot.getChild(tenantId));
            PlatformTenant tenant = toTenant(context, tenantResource, false);
            if (tenant != null) {
                if (tenant.getStatus() == Status.active) {
                    LOG.info("delete->deactivateTenant({})", tenant);
                    doDeactivate(resolver1, context, tenantResource);
                } else {
                    LOG.info("deleteTenant({})", tenant);
                    for (PlatformTenantHook hook : platformHooks) {
                        hook.remove(resolver1, context, tenant);
                    }
                    for (TenantManagerHook hook : managerHooks) {
                        hook.remove(tenant);
                    }
                    resolver1.delete(tenantResource);
                }
            } else {
                throw new PersistenceException("tenant '" + tenantId + "' not found");
            }
            return null;
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
            return manage((ResourceResolverTask<Tenant>)
                    (resolver, context) -> createTenant(resolver, tenantId, properties), null, null);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public final void remove(@Nonnull final Tenant tenant) {
        try {
            manage((ResourceResolverTask<Void>) (resolver, context) -> {
                deleteTenant(resolver, tenant.getId());
                return null;
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public final void setProperty(@Nonnull final Tenant tenant,
                                  @Nonnull final String name, @Nullable final Object value) {
        try {
            change((ResourceResolverTask<Void>) (resolver, context) -> {
                changeTenant(resolver, tenant.getId(), new HashMap<String, Object>() {{
                    put(name, value);
                }});
                return null;
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public final void setProperties(@Nonnull final Tenant tenant,
                                    @Nonnull final Map<String, Object> properties) {
        try {
            change((ResourceResolverTask<Void>) (resolver, context) -> {
                changeTenant(resolver, tenant.getId(), properties);
                return null;
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public final void removeProperties(@Nonnull final Tenant tenant, final String... propertyNames) {
        try {
            change((ResourceResolverTask<Void>) (resolver, context) -> {
                Map<String, Object> properties = new HashMap<>();
                for (String key : propertyNames) {
                    properties.put(key, null);
                }
                changeTenant(resolver, tenant.getId(), properties);
                return null;
            }, null, tenant.getId());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    // TenantProvider

    @Override
    public final Tenant getTenant(@Nonnull final String tenantId) {
        return call((ResourceResolverTask<Tenant>) (resolver, context) -> getTenant(resolver, tenantId), null);
    }

    @Override
    public final Iterator<Tenant> getTenants() {
        return getTenants(null);
    }

    @Override
    public final Iterator<Tenant> getTenants(@Nullable final String tenantFilter) {
        return retrieve((resolver, context) -> {
            ResourceFilter resourceFilter = null;
            if (StringUtils.isNotBlank(tenantFilter)) {
                try {
                    final Filter osgiFilter = FrameworkUtil.createFilter(tenantFilter);
                    resourceFilter = new ResourceFilter.AbstractResourceFilter() {

                        @Override
                        public boolean accept(Resource resource) {
                            return osgiFilter.matches(resource.getValueMap());
                        }

                        @Override
                        public boolean isRestriction() {
                            return true;
                        }

                        @Override
                        public void toString(@Nonnull StringBuilder builder) {
                            builder.append(osgiFilter.toString());
                        }
                    };
                } catch (InvalidSyntaxException ex) {
                    LOG.error(ex.toString());
                }
            }
            return getTenants(resolver, resourceFilter);
        }, null, null);
    }

    //
    // to implement the interface methods driven in various resolver contexts
    //

    /**
     * call action using service resolver if retrieval access is granted otherwise the given resolver...
     */
    protected <T> T retrieve(@Nonnull final ResourceResolverTask<T> task,
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
    protected <T> T change(@Nonnull final ResourceResolverTask<T> task,
                           @Nullable final ResourceResolver context, @Nullable String tenantId)
            throws PersistenceException {
        return call(task, changingGranted, context, tenantId);
    }

    /**
     * call action using service resolver if managing access is granted otherwise the given resolver...
     */
    protected <T> T manage(@Nonnull final ResourceResolverTask<T> task,
                           @Nullable final ResourceResolver context, @Nullable String tenantId)
            throws PersistenceException {
        return call(task, managingGranted, context, tenantId);
    }

    // permission check

    protected final RetrievalGranted retrievalGranted = new RetrievalGranted();
    protected final ChangingGranted changingGranted = new ChangingGranted();
    protected final ManagingGranted managingGranted = new ManagingGranted();

    private final class RetrievalGranted implements PermissionCheck {
        @Override
        public boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId) {
            boolean granted = false;
            try {
                String tenantsRootPath = getTenantsRoot(resolver).getPath();
                granted = resolver.getResource(StringUtils.isBlank(tenantId)
                        ? tenantsRootPath : tenantsRootPath + "/" + tenantId) != null;
            } catch (Exception ignore) {
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("retrievalGranted({},{}): {}", resolver.getUserID(), tenantId, granted);
            }
            return granted;
        }
    }

    private final class ChangingGranted implements PermissionCheck {
        @Override
        public boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId) {
            boolean granted = false;
            try {
                Session session = resolver.adaptTo(Session.class);
                String tenantsRootPath = getTenantsRoot(resolver).getPath();
                granted = StringUtils.isNotBlank(tenantId)
                        && permissionsService.isMemberOfOne(session,
                        "administrators",
                        "composum-platform-administrators",
                        "tenant-" + tenantId + "-managers") != null
                        && permissionsService.hasAllPrivileges(session, tenantsRootPath + "/" + tenantId,
                        "jcr:read");
            } catch (Exception ignore) {
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("changingGranted({},{}): {}", resolver.getUserID(), tenantId, granted);
            }
            return granted;
        }
    }

    private final class ManagingGranted implements PermissionCheck {
        @Override
        public boolean isAccessGranted(@Nonnull ResourceResolver resolver, @Nullable String tenantId) {
            boolean granted = false;
            try {
                Session session = resolver.adaptTo(Session.class);
                String tenantsRootPath = getTenantsRoot(resolver).getPath();
                granted = permissionsService.isMemberOfOne(session,
                        "administrators",
                        "composum-platform-administrators") != null
                        && permissionsService.hasAllPrivileges(session, tenantsRootPath,
                        "rep:write");
            } catch (Exception ignore) {
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("managingGranted({},{}): {}", resolver.getUserID(), tenantId, granted);
            }
            return granted;
        }
    }
}
