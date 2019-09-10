package com.composum.platform.tenant.service;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface HostManagerService {

    InetAddress NO_ADDRESS = InetAddress.getLoopbackAddress();

    abstract class Host implements Comparable<Host> {

        private final String hostname;
        private final boolean configured;
        private final boolean enabled;
        private final boolean certAvailable;
        private final boolean secured;

        private transient InetAddress inetAddress;

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

        public abstract void applyResource(@Nonnull Resource resource);

        public abstract boolean isValid();

        public abstract boolean isLocked();

        public abstract String getSiteRef();

        public String getHostname() {
            return hostname;
        }

        public boolean isConfigured() {
            return configured;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isCertAvailable() {
            return certAvailable;
        }

        public boolean isSecured() {
            return secured;
        }

        public InetAddress getInetAddress() {
            if (inetAddress == null) {
                try {
                    inetAddress = InetAddress.getByName(getHostname());
                } catch (UnknownHostException ignore) {
                }
                if (inetAddress == null) {
                    inetAddress = NO_ADDRESS;
                }
            }
            return inetAddress != NO_ADDRESS ? inetAddress : null;
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

        public boolean contains(@Nonnull final String hostname) {
            for (Host host : this) {
                if (host.getHostname().equals(hostname)) return true;
            }
            return false;
        }
    }

    class ProcessException extends Exception {

        private int exitValue;
        private List<String> errorMessages;

        public ProcessException(String message) {
            this(-1, Collections.singletonList(message));
        }

        public ProcessException(int exitValue, List<String> messages) {
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

    List<Host> hostList(@Nonnull ResourceResolver resolver, @Nullable String tenantId)
            throws ProcessException;

    Host hostStatus(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostCreate(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostEnable(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostDisable(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostCert(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostRevoke(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostSecure(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    Host hostUnsecure(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;

    void hostDelete(@Nonnull ResourceResolver resolver, @Nullable String tenantId, @Nonnull String hostname)
            throws ProcessException;
}
