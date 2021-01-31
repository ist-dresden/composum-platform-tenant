package com.composum.platform.tenant.service.impl;

import com.composum.pages.commons.model.Site;
import com.composum.pages.commons.service.ResourceManager;
import com.composum.pages.commons.service.SiteManager;
import com.composum.platform.cache.service.CacheConfiguration;
import com.composum.platform.cache.service.CacheManager;
import com.composum.platform.cache.service.impl.CacheServiceImpl;
import com.composum.platform.commons.request.AccessMode;
import com.composum.platform.tenant.service.HostManagerService;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Host Manager"
        }
)
@Designate(ocd = PlatformHostManager.Configuration.class)
public final class PlatformHostManager extends CacheServiceImpl<List<InetAddress>> implements HostManagerService {

    public static final String PN_SITE_REF = "siteRef";
    public static final String PN_SITE_STAGE = "siteStage";
    public static final String PN_LOCKED = "locked";

    public static final String SP_RESOLVER_MAP_LOCATION = "resource.resolver.map.location";

    private static final Logger LOG = LoggerFactory.getLogger(PlatformHostManager.class);

    @ObjectClassDefinition(
            name = "Composum Platform Host Configuration"
    )
    @interface Configuration {

        @AttributeDefinition(
                name = "public system host",
                description = "the systems public host name; default: the result of the 'hostname' call"
        )
        String public_system_host();

        @AttributeDefinition(
                name = "hostname cmd",
                description = "the system command to get the systems hostname; default: '/usr/bin/hostname'"
        )
        String hostname_cmd() default "/usr/bin/hostname";

        @AttributeDefinition(
                name = "Host command",
                description = "the system command to manage hosts; default: '/etc/httpd/bin/host'"
        )
        String host_manage_cmd() default "/etc/httpd/bin/host";

        @AttributeDefinition(
                name = "Mapping Location",
                description = "root of the configuration of the ResourceResolver mapping; default: '/etc/map'"
        )
        String resource_resolver_map_location() default "/etc/map";

        @AttributeDefinition(
                name = "Mapping Template",
                description = "the tempate to create ResourceResolver mapping; default: '/conf/composum/platform/tenant/map/template'"
        )
        String resource_resolver_map_template() default "/conf/composum/platform/tenant/map/template";
    }

    public final static Map<String, Object> MAP_FOLDER_PROPS = new HashMap<String, Object>() {{
        put(JcrConstants.JCR_PRIMARYTYPE, ResourceUtil.TYPE_SLING_FOLDER);
    }};

    public final static Map<String, Object> HOSTS_PROPS = new HashMap<String, Object>() {{
        put(JcrConstants.JCR_PRIMARYTYPE, ResourceUtil.TYPE_SLING_FOLDER);
    }};

    public final static Map<String, Object> HOST_PROPS = new HashMap<String, Object>() {{
        put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
    }};

    protected class PlatformHost extends Host {

        private String siteRef;
        private String siteStage;

        private boolean locked;

        private transient String reversedName;

        protected PlatformHost(@Nonnull final String hostname) {
            this(hostname, false, false, false, false);
        }

        protected PlatformHost(@Nonnull final String hostname,
                               final boolean configured,
                               final boolean enabled,
                               final boolean cert,
                               final boolean secured) {
            super(hostname, configured, enabled, cert, secured);
        }

        public void applyResource(@Nonnull final Resource resource) {
            ValueMap values = resource.getValueMap();
            siteRef = values.get(PN_SITE_REF, String.class);
            siteStage = values.get(PN_SITE_STAGE, String.class);
            locked = values.get(PN_LOCKED, Boolean.FALSE);
        }

        @Override
        public boolean isAvailable() {
            return isEnabled() && isValid();
        }

        @Override
        public boolean isValid() {
            if (publicIpAddresses != null && publicIpAddresses.size() > 0) {
                List<InetAddress> addresses = getInetAddresses();
                if (addresses != null && addresses.size() > 0) {
                    for (InetAddress adr : addresses) {
                        if (!publicIpAddresses.contains(adr)) {
                            return false; // the whole set must be equal
                        }
                    }
                    return true; // address set is equal
                }
            }
            return false; // at least one address set is empty
        }

        @Override
        public boolean isLocked() {
            return locked;
        }

        @Override
        @Nullable
        public String getSiteRef() {
            return siteRef;
        }

        @Override
        @Nullable
        public String getSiteStage() {
            return siteStage;
        }

        @Override
        public int compareTo(@Nonnull final Host other) {
            return getReversedName().compareTo(((PlatformHost) other).getReversedName());
        }

        @Nonnull
        protected String getReversedName() {
            if (reversedName == null) {
                String[] segments = StringUtils.split(getHostname(), ".");
                ArrayUtils.reverse(segments);
                reversedName = StringUtils.join(segments, ".");
            }
            return reversedName;
        }

        @Override
        @Nonnull
        protected List<InetAddress> fetchInetAddresses(@Nonnull final String hostname) {
            List<InetAddress> adresses = get(hostname);
            if (adresses == null) {
                adresses = super.fetchInetAddresses(hostname);
                put(hostname, adresses);
            }
            return adresses;
        }

        @Override
        public String toString() {
            return getHostname();
        }

        @Override
        public int hashCode() {
            return getHostname().hashCode();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Host && getHostname().equals(((Host) object).getHostname());
        }
    }

    @Reference
    protected ResourceResolverFactory resolverFactory;

    @Reference
    protected CacheManager cacheManager;

    @Reference
    protected ResourceManager resourceManager;

    @Reference
    protected SiteManager siteManager;

    @Reference
    protected TenantManagerService tenantManager;

    @Reference
    protected TenantUserManager userManager;

    protected PlatformHostManager.Configuration config;

    protected String publicHostname;
    protected List<InetAddress> publicIpAddresses;
    protected String resolverMapLocation;

    @SuppressWarnings("ClassExplicitlyAnnotation")
    public static class InetAddressCacheConfig implements CacheConfiguration {

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public String name() {
            return "ComposumPlatformHostInetAddresses";
        }

        @Override
        public String contentType() {
            return List.class.getName();
        }

        @Override
        public int maxElementsInMemory() {
            return 500;
        }

        @Override
        public int timeToLiveSeconds() {
            return 3600;
        }

        @Override
        public int timeToIdleSeconds() {
            return 1200;
        }

        @Override
        public String webconsole_configurationFactory_nameHint() {
            return "Host Inet Adresses (heap: 500, time: 1200-3600)";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return CacheConfiguration.class;
        }
    }

    @Activate
    @Modified
    protected void activate(BundleContext bundleContext, PlatformHostManager.Configuration config) {
        this.config = config;
        super.activate(cacheManager, new InetAddressCacheConfig());
        publicIpAddresses = null;
        publicHostname = config.public_system_host();
        if (StringUtils.isBlank(publicHostname)) {
            publicHostname = callCmd(config.hostname_cmd());
        }
        try {
            publicIpAddresses = Arrays.asList(InetAddress.getAllByName(publicHostname));
        } catch (UnknownHostException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        LOG.info("activate(): {} / {} ({})", publicHostname, StringUtils.join(publicIpAddresses, ","),
                config.resource_resolver_map_location());
    }

    @Override
    public String getPublicHostname() {
        return publicHostname;
    }

    @Override
    public HostList hostList(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId)
            throws ProcessException {
        checkPermissions(resolver, tenantId, null, false);
        HostList result = new HostList();
        if (StringUtils.isNotBlank(tenantId)) {
            try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
                final Resource tenantsRoot = tenantManager.getTenantsRoot(serviceResolver);
                Resource tenantRes = tenantsRoot.getChild(tenantId);
                if (tenantRes != null) {
                    Resource hostsRes = tenantRes.getChild("hosts");
                    if (hostsRes != null) {
                        for (Resource hostRes : hostsRes.getChildren()) {
                            PlatformHost host = (PlatformHost) getStatus(hostRes.getName());
                            if (host == null) {
                                host = new PlatformHost(hostRes.getName());
                            }
                            host.applyResource(hostRes);
                            result.add(host);
                        }
                    }
                }
            } catch (LoginException ex) {
                LOG.error(ex.getMessage());
            }
        } else {
            hostManageCmd(reader -> {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] status = StringUtils.split(line, " ");
                    result.add(new PlatformHost(
                            status[0], true,
                            status[1].equals("enabled"),
                            status[2].equals("cert"),
                            status[3].equals("secured")
                    ));
                }
            }, "list", "status", false);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("hostList({}): {}", tenantId != null ? tenantId : "", StringUtils.join(result, ", "));
        }
        Collections.sort(result);
        return result;
    }

    private Host getStatus(@Nonnull final String hostname) {
        List<Host> result = new ArrayList<>();
        try {
            hostManageCmd(reader -> {
                String line;
                if ((line = reader.readLine()) != null) {
                    String[] status = StringUtils.split(line, " ");
                    result.add(new PlatformHost(
                            status[0], true,
                            status[1].equals("enabled"),
                            status[2].equals("cert"),
                            status[3].equals("secured")
                    ));
                }
            }, "status", hostname, false);
        } catch (ProcessException ignore) {
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("hostStatus({}): {}", hostname, StringUtils.join(result, ", "));
        }
        return result.size() > 0 ? result.get(0) : null;
    }


    @Override
    public Host hostStatus(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, false);
        return getStatus(hostname);
    }

    // tenant hosts management

    protected void checkHostname(@Nullable final String hostname) throws ProcessException {
        if (StringUtils.isBlank(hostname) || !HOSTNAME_PATTERN.matcher(hostname).matches()) {
            LOG.error("invalid hostname '{}'", hostname);
            throw new ProcessException("invalid hostname");
        }
    }

    private Resource getHostNode(@Nonnull final ResourceResolver serviceResolver,
                                 @Nonnull final String tenantId, @Nonnull final String hostname) {
        Resource hostNode = null;
        final Resource tenantsRoot = tenantManager.getTenantsRoot(serviceResolver);
        final Resource tenantResource = Objects.requireNonNull(tenantsRoot.getChild(tenantId));
        Resource hostsNode = tenantResource.getChild("hosts");
        if (hostsNode != null) {
            hostNode = hostsNode.getChild(hostname);
        }
        return hostNode;
    }

    @Override
    public void removeHost(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException, PersistenceException {
        checkPermissions(resolver, tenantId, hostname, true);
        Tenant tenant = tenantManager.getTenant(resolver, tenantId);
        if (tenant == null) {
            throw new ProcessException("tenant '" + tenantId + "' not available");
        }
        checkHostname(hostname);
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            Resource hostNode = getHostNode(serviceResolver, tenantId, hostname);
            if (hostNode != null) {
                hostDelete(resolver, tenantId, hostname);
                serviceResolver.delete(hostNode);
                serviceResolver.commit();
            }
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
            throw new ProcessException("action not allowed");
        }
    }

    @Override
    public Host addHost(@Nonnull final ResourceResolver resolver, @Nonnull final String tenantId,
                        @Nonnull final String hostname)
            throws ProcessException, PersistenceException {
        checkPermissions(resolver, tenantId, hostname, false);
        Tenant tenant = tenantManager.getTenant(resolver, tenantId);
        if (tenant == null) {
            throw new ProcessException("tenant '" + tenantId + "' not available");
        }
        checkHostname(hostname);
        String ownerId = getHostsTenant(resolver, hostname);
        if (ownerId != null) {
            if (!ownerId.equals(tenantId)) {
                throw new ProcessException("hostname is already in use by another tenant");
            } else {
                return getStatus(hostname);
            }
        }
        String domainOwner = getDomainOwner(resolver, hostname);
        if (domainOwner != null && !domainOwner.equals(tenantId) && !"admin".equals(resolver.getUserID())) {
            throw new ProcessException("hostname is reserved by another tenant");
        }
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            final Resource tenantsRoot = tenantManager.getTenantsRoot(serviceResolver);
            final Resource tenantResource = Objects.requireNonNull(tenantsRoot.getChild(tenantId));
            Resource hostsNode = tenantResource.getChild("hosts");
            if (hostsNode == null) {
                hostsNode = serviceResolver.create(tenantResource, "hosts", HOSTS_PROPS);
            }
            Resource hostNode = serviceResolver.create(hostsNode, hostname, HOST_PROPS);
            serviceResolver.commit();
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
            throw new ProcessException("action not allowed");
        }
        return getStatus(hostname);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    protected String getHostsTenant(@Nonnull final ResourceResolver resolver, @Nonnull final String hostname) {
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            String tenantsRoot = tenantManager.getTenantsRoot(serviceResolver).getPath();
            String query = ("/jcr:root" + tenantsRoot + "/*/hosts/" + hostname);
            Iterator<Resource> found = serviceResolver.findResources(query, Query.XPATH);
            if (found.hasNext()) {
                Resource hostRes = found.next();
                return Objects.requireNonNull(Objects.requireNonNull(hostRes.getParent()).getParent()).getName();
            }
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    protected String getDomainOwner(@Nonnull final ResourceResolver resolver, @Nonnull final String hostname) {
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            String tenantsRoot = tenantManager.getTenantsRoot(serviceResolver).getPath();
            String domain = hostname;
            while ((domain = StringUtils.substringAfter(domain, ".")).contains(".")) {
                String query = ("/jcr:root" + tenantsRoot + "/*/hosts[domains='" + domain + "']");
                Iterator<Resource> found = serviceResolver.findResources(query, Query.XPATH);
                if (found.hasNext()) {
                    Resource hostsRes = found.next();
                    return Objects.requireNonNull(hostsRes.getParent()).getName();
                }
            }
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    // site mapping

    /**
     * assigns a site to a host
     *
     * @param context   the context to access the site
     * @param tenantId  the tenants identifier
     * @param hostname  the name of the host to assign
     * @param siteRef   the path of the hosts site; if 'null' each assignment is removed
     * @param siteStage the key of the stage to map
     */
    @Override
    public Host assignSite(@Nonnull final BeanContext context,
                           @Nonnull final String tenantId, @Nonnull final String hostname,
                           @Nullable final String siteRef, @Nullable final String siteStage)
            throws ProcessException, PersistenceException {
        if (LOG.isInfoEnabled()) {
            LOG.info("assignSite '{}' - '{}/{}'", hostname, siteRef, siteStage);
        }
        ResourceResolver resolver = context.getResolver();
        checkPermissions(resolver, tenantId, hostname, true);
        checkHostname(hostname);
        checkLocked(hostname);
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            Resource hostNode = getHostNode(serviceResolver, tenantId, hostname);
            if (hostNode != null) {
                Resource siteRes = StringUtils.isNotBlank(siteRef) ? serviceResolver.getResource(siteRef) : null;
                Site site;
                if (siteRes != null && (site = siteManager.createBean(context, siteRes)) != null) {
                    PlatformTenant tenant = (PlatformTenant) tenantManager.getTenant(serviceResolver, tenantId);
                    if (tenant != null) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("createSiteMapping '{}' - '{}/{}'", hostname, siteRef, siteStage);
                        }
                        createSiteMapping(serviceResolver, tenant, hostNode, site, siteStage);
                    } else {
                        LOG.error("invalid tanant '{}'", tenantId);
                        throw new ProcessException("invalid tenant '{" + tenantId + "}'");
                    }
                } else {
                    if (StringUtils.isNotBlank(siteRef)) {
                        LOG.error("invalid site '{}'", siteRef);
                        throw new ProcessException("invalid site '{" + siteRef + "}'");
                    }
                    if (LOG.isInfoEnabled()) {
                        LOG.info("deleteSiteMapping '{}' - '{}/{}'", hostname, siteRef, siteStage);
                    }
                    deleteSiteMapping(serviceResolver, hostNode);
                }
                serviceResolver.commit();
                return hostStatus(resolver, tenantId, hostname);
            } else {
                LOG.error("host not configured: '{}'", hostname);
                throw new ProcessException("host not configured: '" + hostname + "'");
            }
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
            throw new ProcessException("action not allowed");
        }
    }

    protected class MappingTemplateContext implements ResourceManager.TemplateContext {

        private final ResourceResolver resolver;
        private final String hostname;
        private final String hostPattern;
        private final PlatformTenant tenant;
        private final Site site;
        private final String stage;
        private final String stagePath;

        public MappingTemplateContext(@Nonnull final ResourceResolver resolver, @Nonnull final String hostname,
                                      @Nonnull final PlatformTenant tenant, @Nonnull final Site site, @Nonnull final String stage) {
            this.resolver = resolver;
            this.hostname = hostname;
            this.hostPattern = hostname.replaceAll("\\.", "\\\\\\\\.");
            this.tenant = tenant;
            this.site = site;
            this.stage = stage;
            this.stagePath = site.getStagePath(AccessMode.valueOf(stage.toUpperCase()));
        }

        @Override
        public ResourceResolver getResolver() {
            return resolver;
        }

        @Override
        public String applyTemplatePlaceholders(@Nonnull Resource target, @Nonnull String value) {
            String result = value;
            result = result.replaceAll("\\$\\{host}", hostname);
            result = result.replaceAll("\\$\\{hostPattern}", hostPattern);
            result = result.replaceAll("\\$\\{apps}", tenant.getApplicationRoot());
            result = result.replaceAll("\\$\\{content}", stagePath);
            if (!value.equals(result)) {
                result = result.replaceAll("/[^/]+/\\.\\./", "/");
            }
            return result;
        }
    }

    private void createSiteMapping(@Nonnull final ResourceResolver resolver, @Nonnull final PlatformTenant tenant,
                                   @Nonnull final Resource hostNode, @Nonnull final Site site, @Nullable String siteStage)
            throws ProcessException {
        Resource mapRoot = getConfigResource(resolver, config.resource_resolver_map_location());
        try {
            String siteRef = site.getPath();
            if (StringUtils.isBlank(siteStage)) {
                siteStage = AccessMode.ACCESS_MODE_PUBLIC.toLowerCase();
            }
            String assignedHost = getSiteHost(siteRef, siteStage);
            if (assignedHost != null && !assignedHost.equals(hostNode.getName())) {
                throw new ProcessException("site / stage: '" + siteRef + "' / '" + siteStage + "' already assigned to a host");
            }
            deleteSiteMapping(resolver, hostNode);
            ModifiableValueMap values = hostNode.adaptTo(ModifiableValueMap.class);
            if (values != null) {
                values.put(PN_SITE_STAGE, siteStage);
                values.put(PN_SITE_REF, siteRef);
                String hostname = hostNode.getName();
                Resource template = getConfigResource(resolver, config.resource_resolver_map_template());
                MappingTemplateContext templateContext = new MappingTemplateContext(resolver, hostname, tenant, site, siteStage);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createHttpMapping '{}' - '{}/{}'", hostname, siteRef, siteStage);
                }
                Resource mapFolder = getMapFolder(resolver, "http");
                resourceManager.createFromTemplate(templateContext, mapFolder, hostname, template, false);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("createHttpsMapping '{}' - '{}/{}'", hostname, siteRef, siteStage);
                }
                mapFolder = getMapFolder(resolver, "https");
                resourceManager.createFromTemplate(templateContext, mapFolder, hostname, template, false);
            } else {
                throw new PersistenceException("host configuration not modifiable (" + hostNode.getName() + ")");
            }
        } catch (PersistenceException | RepositoryException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ProcessException(ex.getMessage());
        }
    }

    private void deleteSiteMapping(@Nonnull final ResourceResolver resolver, @Nonnull final Resource hostNode)
            throws ProcessException {
        Resource mapRoot = getConfigResource(resolver, config.resource_resolver_map_location());
        try {
            ModifiableValueMap values = hostNode.adaptTo(ModifiableValueMap.class);
            if (values != null) {
                values.remove(PN_SITE_STAGE);
                values.remove(PN_SITE_REF);
                Resource mapRes = mapRoot.getChild("http/" + hostNode.getName());
                if (mapRes != null) {
                    resolver.delete(mapRes);
                }
                mapRes = mapRoot.getChild("https/" + hostNode.getName());
                if (mapRes != null) {
                    resolver.delete(mapRes);
                }
            } else {
                throw new PersistenceException("host configuration not modifiable (" + hostNode.getName() + ")");
            }
        } catch (PersistenceException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ProcessException(ex.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    protected String getSiteHost(@Nonnull final String siteRef, @Nonnull final String siteStage) {
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            String tenantsRoot = tenantManager.getTenantsRoot(serviceResolver).getPath();
            String query = ("/jcr:root" + tenantsRoot + "/*/hosts/*"
                    + "[siteRef='" + siteRef + "' and siteStage='" + siteStage + "']");
            Iterator<Resource> found = serviceResolver.findResources(query, Query.XPATH);
            if (found.hasNext()) {
                Resource hostRes = found.next();
                return hostRes.getName();
            }
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    protected Resource getMapFolder(@Nonnull final ResourceResolver resolver, String name)
            throws ProcessException, PersistenceException {
        Resource mapRoot = getConfigResource(resolver, config.resource_resolver_map_location());
        Resource folder = mapRoot.getChild(name);
        if (folder == null) {
            folder = resolver.create(mapRoot, name, MAP_FOLDER_PROPS);
        }
        return folder;
    }

    @Nonnull
    protected Resource getConfigResource(@Nonnull final ResourceResolver resolver, String path)
            throws ProcessException {
        Resource configRes = resolver.getResource(path);
        if (configRes != null) {
            return configRes;
        } else {
            LOG.error("invalid configuration '{}'", path);
            throw new ProcessException("invalid service configuration");
        }
    }

    // server host configuration

    @Override
    public Host hostCreate(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "create", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostCreate({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public Host hostEnable(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "enable", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostEnable({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public Host hostDisable(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                            @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "disable", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostDisable({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public Host hostCert(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                         @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "cert", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostCert({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public Host hostRevoke(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "revoke", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostRevoke({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public Host hostSecure(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "secure", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostSecure({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public Host hostUnsecure(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                             @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "unsecure", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostUnsecure({}): {}", hostname, exitValue);
        }
        return hostStatus(resolver, tenantId, hostname);
    }

    @Override
    public void hostDelete(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                           @Nonnull final String hostname)
            throws ProcessException {
        checkPermissions(resolver, tenantId, hostname, true);
        int exitValue = hostManageCmd(null, "delete", hostname, true);
        if (LOG.isInfoEnabled()) {
            LOG.info("hostDelete({}): {}", hostname, exitValue);
        }
    }

    /**
     * check the permissions to perform a host managment operation
     *
     * @param resolver           the resolver for the transaction
     * @param tenantId           the tenants id
     * @param hostname           the name of the host
     * @param hostMustBeAssigned 'true' if the host must be assigned to the tenant
     */
    private void checkPermissions(@Nonnull final ResourceResolver resolver, @Nullable final String tenantId,
                                  @Nullable final String hostname, boolean hostMustBeAssigned)
            throws ProcessException {
        String userId = resolver.getUserID();
        if (StringUtils.isBlank(userId) || (!"admin".equals(userId) &&
                (StringUtils.isBlank(tenantId) ||
                        !userManager.isInRole(tenantId, TenantUserManager.Role.manager, userId) ||
                        (hostMustBeAssigned && (hostname == null || !hostList(resolver, tenantId).contains(hostname)))))) {
            LOG.error("permissions.failure:{},{},{},{}", hostname, userId, tenantId,
                    StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(userId)
                            ? userManager.isInRole(tenantId, TenantUserManager.Role.manager, userId) : "?");
            throw new ProcessException("insufficient permissions");
        }
    }

    private interface OutputHandler {
        void slurpIt(BufferedReader reader) throws IOException;
    }

    private void checkLocked(@Nullable final String hostname)
            throws ProcessException {
        if (StringUtils.isNotBlank(hostname)) {
            Host host = getStatus(hostname);
            if (host != null && host.isLocked()) {
                throw new ProcessException("host is locked");
            }
        }
    }

    private int hostManageCmd(@Nullable final OutputHandler out, @Nonnull final String operation,
                              @Nullable final String hostname, boolean checkLocked)
            throws ProcessException {
        int exitValue;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("hostManageCmd: {} {}", operation, hostname);
            }
            if (StringUtils.isNotBlank(hostname) && !"status".equals(hostname)) {
                if (!HOSTNAME_PATTERN.matcher(hostname).matches()) {
                    LOG.error("invalid hostname '{}' on executing manage cmd '{}'", hostname, operation);
                    throw new ProcessException("invalid hostname");
                }
            }
            if (checkLocked) {
                checkLocked(hostname);
            }
            Process process = new ProcessBuilder().command(config.host_manage_cmd(), operation, hostname).start();
            if (out != null) {
                BufferedReader processOut = new BufferedReader(new
                        InputStreamReader(process.getInputStream()));
                out.slurpIt(processOut);
            }
            List<String> errorLines = new ArrayList<>();
            BufferedReader processErr = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = processErr.readLine()) != null) {
                errorLines.add(line);
            }
            boolean ok = process.waitFor(15, TimeUnit.SECONDS);
            if (ok) {
                exitValue = process.exitValue();
            } else {
                exitValue = -1;
                errorLines.add("process execution timed out");
            }
            if (exitValue != 0) {
                LOG.error("process exited with '{}': {}", exitValue, StringUtils.join(errorLines, ", "));
                throw new ProcessException(exitValue, errorLines);
            }
        } catch (IOException | InterruptedException ex) {
            LOG.error(ex.toString());
            throw new ProcessException(ex.getMessage());
        }
        return exitValue;
    }

    private String callCmd(String... cmd) {
        StringBuilder result = new StringBuilder();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("callCmd: {}", StringUtils.join(cmd, " "));
            }
            Process process = new ProcessBuilder().command(cmd).start();
            BufferedReader processOut = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
            String line;
            while ((line = processOut.readLine()) != null) {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
        } catch (IOException ex) {
            LOG.warn(ex.getMessage());
        }
        return result.toString();
    }
}
