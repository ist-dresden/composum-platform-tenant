package com.composum.platform.tenant.service;

import com.composum.sling.core.BeanContext;
import com.composum.sling.core.util.XSS;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public interface HostManagerService {

    Pattern HOSTNAME_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$"
    );

    List<InetAddress> NO_ADDRESS = Collections.emptyList();

    String VALUE_HOSTNAME = "hostname";
    String VALUE_ADDRESS = "address";
    String VALUE_SITE = "site";
    String VALUE_STAGE = "stage";
    String VALUE_VALID = "valid";
    String VALUE_CONFIGURED = "configured";
    String VALUE_ENABLED = "enabled";
    String VALUE_CERTIFICATE = "certificate";
    String VALUE_SECURED = "secured";
    String VALUE_LOCKED = "locked";

    abstract class Host implements Comparable<Host> {

        private final String hostname;
        private final boolean configured;
        private final boolean enabled;
        private final boolean certAvailable;
        private final boolean secured;

        private transient String reversedName;
        private transient List<InetAddress> inetAddresses;

        public Host(@Nonnull String hostname,
                    boolean configured,
                    boolean enabled,
                    boolean cert,
                    boolean secured) {
            this.hostname = hostname;
            this.configured = configured;
            this.enabled = enabled;
            this.certAvailable = cert;
            this.secured = secured;
        }

        /**
         * @return 'true' if the host is really usable and routing to a site
         */
        public abstract boolean isAvailable();

        /**
         * @return 'true' if the host is routing to this system
         */
        public abstract boolean isValid();

        /**
         * @return 'true' if the host is not managed by this host manager
         */
        public abstract boolean isForeignHost();

        /**
         * @return 'true' if an admin has locked the host to prevent from changes
         */
        public abstract boolean isLocked();

        /**
         * @return an assigned message
         */
        @Nullable
        public abstract String getMessage();

        /**
         * @return the expected inet address
         */
        @Nonnull
        public abstract List<InetAddress> getExpectedAddresses();

        /**
         * @return the path of the assigned site
         */
        @Nullable
        public abstract String getSiteRef();

        @Nullable
        public abstract String getSiteStage();

        @Nonnull
        public String getHostname() {
            return hostname;
        }

        /**
         * @return 'true' if the host is configured in the Webserver
         */
        public boolean isConfigured() {
            return configured;
        }

        /**
         * @return 'true' if the host configuration is switched 'on'
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @return 'true' if there is a SSL certificate available for this hist
         */
        public boolean isCertAvailable() {
            return certAvailable;
        }

        /**
         * @return 'true' if the host supports SSL and is routung non-SSL to SSL
         */
        public boolean isSecured() {
            return secured;
        }

        @Nullable
        public String getAddress() {
            List<InetAddress> addresses = getInetAddresses();
            return addresses != null && addresses.size() > 0 ? addresses.get(0).getHostAddress() : null;
        }

        @Nullable
        public List<InetAddress> getInetAddresses() {
            if (inetAddresses == null) {
                inetAddresses = fetchInetAddresses(getHostname());
            }
            return inetAddresses != NO_ADDRESS ? inetAddresses : null;
        }

        @Nonnull
        protected List<InetAddress> fetchInetAddresses(@Nonnull final String hostname) {
            try {
                return Arrays.asList(InetAddress.getAllByName(hostname));
            } catch (UnknownHostException ex) {
                return NO_ADDRESS;
            }
        }

        public String getId() {
            return XSS.filter(getHostname().replace('.', '°'));
        }

        /**
         * @return the base64 encoded JSON data of the host
         */
        @Nonnull
        public String getEncodedData() {
            return Base64.encodeBase64String(toJson().toString().getBytes(StandardCharsets.UTF_8));
        }

        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            List<InetAddress> addresses = getInetAddresses();
            object.addProperty(VALUE_HOSTNAME, getHostname());
            if (addresses != null) {
                JsonArray hostIps = new JsonArray();
                for (InetAddress adr : addresses) {
                    hostIps.add(adr.getHostAddress());
                }
                object.add(VALUE_ADDRESS, hostIps);
            }
            object.addProperty(VALUE_SITE, getSiteRef());
            object.addProperty(VALUE_STAGE, getSiteStage());
            object.addProperty(VALUE_VALID, isValid());
            object.addProperty(VALUE_ENABLED, isEnabled());
            object.addProperty(VALUE_CONFIGURED, isConfigured());
            object.addProperty(VALUE_CERTIFICATE, isCertAvailable());
            object.addProperty(VALUE_SECURED, isSecured());
            object.addProperty(VALUE_LOCKED, isLocked());
            return object;
        }

        @Override
        public int compareTo(@Nonnull final Host other) {
            return getReversedName().compareTo(other.getReversedName());
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
        public String toString() {
            return hostname;
        }

        @Override
        public int hashCode() {
            return hostname.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Host && hostname.equals(object);
        }
    }

    class HostList extends ArrayList<Host> {

        public boolean contains(@Nonnull String hostname) {
            return get(hostname) != null;
        }

        public Host get(@Nonnull String hostname) {
            for (Host host : this) {
                if (host.getHostname().equals(hostname)) return host;
            }
            return null;
        }
    }

    class ProcessException extends Exception {

        private int exitValue;
        private List<String> errorMessages;

        public ProcessException(String message) {
            super(message);
            this.exitValue = -1;
            this.errorMessages = Collections.singletonList(message);
        }

        public ProcessException(int exitValue, List<String> messages) {
            super(messages + " (exit " + exitValue + ")");
            this.exitValue = exitValue;
            this.errorMessages = messages;
        }

        public int getExitValue() {
            return exitValue;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }
    }

    String getPublicHostname();

    /**
     * retrieves the list of hosts for one tenant or the list of configured hosts if tenant is 'null'
     */
    HostList hostList(@Nonnull ResourceResolver resolver, @Nullable String tenantId, boolean clearCache)
            throws ProcessException;

    /**
     * retrieves the data of one hosts for a tenant or of a configured hosts if tenant is 'null'
     */
    Host hostStatus(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    // tenant hosts management

    /**
     * joins a host to a tenant
     */
    Host addHost(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                 @Nonnull String hostname)
            throws ProcessException, PersistenceException;

    /**
     * removes a host from a tenant and deletes all host configurations including certificates
     */
    void removeHost(@Nonnull ResourceResolver resolver, @Nonnull String tenantId,
                    @Nonnull String hostname)
            throws ProcessException, PersistenceException;

    // site mapping

    /**
     * @param context   the context to access the site
     * @param tenantId  the tenants identifier
     * @param hostname  the name of the host to assign
     * @param siteRef   the path of the hosts site; if 'null' each assignment is removed
     * @param siteStage the key of the stage to map
     */
    Host assignSite(@Nonnull BeanContext context, @Nonnull String tenantId, @Nonnull String hostname,
                    @Nullable String siteRef, @Nullable String siteStage)
            throws ProcessException, PersistenceException;

    // server host configuration

    /**
     * creates the Webserver configuration of a hosts (if not present; the tenant is used to check the permissions)
     */
    Host hostCreate(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * is switchng 'on' the Webservers configuration of a hosts (if not always 'on'; the tenant is used to check the permissions)
     */
    Host hostEnable(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * is switchng 'off' the Webservers configuration of a hosts (if not always 'off'; the tenant is used to check the permissions)
     */
    Host hostDisable(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * is requesting a SSL certificate for a hosts (if not present always; the tenant is used to check the permissions)
     */
    Host hostCert(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * is revoking the SSL certificate for a hosts (if there is a certificate; the tenant is used to check the permissions)
     */
    Host hostRevoke(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * is securing a hosts - configures SSL (if not always secured; the tenant is used to check the permissions)
     */
    Host hostSecure(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * is unsecuring a hosts - removes SSL configuration (if not always unsecure; the tenant is used to check the permissions)
     */
    Host hostUnsecure(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    /**
     * deletes the Webserver configuration of a hosts (if present; the tenant is used to check the permissions);
     * includes the host disabling and a revoke of the hosts SSL certificate - both if necessary
     */
    void hostDelete(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;
}
