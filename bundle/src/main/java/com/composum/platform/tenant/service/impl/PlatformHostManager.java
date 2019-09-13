package com.composum.platform.tenant.service.impl;

import com.composum.platform.tenant.service.HostManagerService;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.LoginException;
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
import javax.jcr.query.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public final class PlatformHostManager implements HostManagerService {

    public static final String PN_SITE_REF = "siteRef";
    public static final String PN_LOCKED = "locked";

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
    }

    public final static Map<String, Object> HOSTS_PROPS = new HashMap<String, Object>() {{
        put(JcrConstants.JCR_PRIMARYTYPE, ResourceUtil.TYPE_SLING_FOLDER);
    }};

    public final static Map<String, Object> HOST_PROPS = new HashMap<String, Object>() {{
        put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
    }};

    protected class PlatformHost extends Host {

        private String siteRef;
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
        public String getSiteRef() {
            return siteRef;
        }

        @Override
        public int compareTo(@Nonnull final Host other) {
            return getReversedName().compareTo(((PlatformHost) other).getReversedName());
        }

        protected String getReversedName() {
            if (reversedName == null) {
                String[] segments = StringUtils.split(getHostname(), ".");
                ArrayUtils.reverse(segments);
                reversedName = StringUtils.join(segments, ".");
            }
            return reversedName;
        }
    }

    @Reference
    protected ResourceResolverFactory resolverFactory;

    @Reference
    protected TenantManagerService tenantManager;

    @Reference
    protected TenantUserManager userManager;

    protected PlatformHostManager.Configuration config;

    protected String publicHostname;
    protected List<InetAddress> publicIpAddresses;

    @Activate
    @Modified
    protected void activate(BundleContext bundleContext, PlatformHostManager.Configuration config) {
        this.config = config;
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
        LOG.info("activate(): {} / {}", publicHostname, StringUtils.join(publicIpAddresses, ","));
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
            final Resource tenantsRoot = tenantManager.getTenantsRoot(serviceResolver);
            final Resource tenantResource = Objects.requireNonNull(tenantsRoot.getChild(tenantId));
            Resource hostsNode = tenantResource.getChild("hosts");
            if (hostsNode != null) {
                Resource hostNode = hostsNode.getChild(hostname);
                if (hostNode != null) {
                    hostDelete(resolver, tenantId, hostname);
                    serviceResolver.delete(hostNode);
                    serviceResolver.commit();
                }
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
        if (domainOwner != null && !domainOwner.equals(tenantId)) {
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
            if (checkLocked && StringUtils.isNotBlank(hostname)) {
                Host host = getStatus(hostname);
                if (host != null && host.isLocked()) {
                    throw new ProcessException("host is locked");
                }
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
            boolean ok = process.waitFor(5, TimeUnit.SECONDS);
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
            LOG.error(ex.getMessage(), ex);
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
