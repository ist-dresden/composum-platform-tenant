package com.composum.platform.tenant.service.impl;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;

/**
 * a tenant object is serializable; all properties are copied
 */
public class PlatformTenant implements Tenant, Serializable {

    public static final String PN_STATUS = "tenant.status";
    public static final String PN_CONTENT_ROOT = "tenant.content_root";
    public static final String PN_APPLICATION_ROOT = "tenant.application_root";
    public static final String PN_PRINCIPAL_BASE = "tenant.princpal_base";

    public static final String CPM_CREATED = "cpm.created";
    public static final String CPM_DEACTIVATED = "cpm.deactivated";
    public static final String CPM_ACTIVATED = "cpm.activated";

    public enum Status {active, deactivated}

    final private String id;
    final private Status status;
    final private ValueMap properties;

    public PlatformTenant(String id, Status status, ValueMap properties) {
        this.id = id;
        this.properties = properties;
        this.status = status;
    }

    public boolean isActive() {
        return getStatus() == Status.active;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    @Nonnull
    public String getId() {
        return id;
    }

    @Override
    @Nullable
    public String getName() {
        return (String) getProperty(PROP_NAME);
    }

    @Override
    @Nullable
    public String getDescription() {
        return (String) getProperty(PROP_DESCRIPTION);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public <Type> Type getProperty(String name, Type defaultValue) {
        return properties.get(name, defaultValue);
    }

    @Override
    @Nonnull
    public Iterator<String> getPropertyNames() {
        return properties.keySet().iterator();
    }

    /**
     * normally: '/content/{name}'
     */
    @Nonnull
    public String getContentRoot() {
        return getProperty(PN_CONTENT_ROOT, "/content/" + getId());
    }

    /**
     * normally: '/apps/{name}'
     */
    @Nonnull
    public String getApplicationRoot() {
        return getProperty(PN_APPLICATION_ROOT, "/apps/" + getId());
    }

    /**
     * normally: 'tenants/{id}'
     * tenant groups:
     * - 'tenant-{id}-members'      // all joined users
     * - 'tenant-{id}-visitors'     // has read access to the preview area
     * - 'tenant-{id}-editors'      // has content read/write access
     * - 'tenant-{id}-publishers'   // has content read access an the right to publish content
     * - 'tenant-{id}-developers'   // has write access to the tenants application area
     * - 'tenant-{id}-managers'     // can mange users of the tenant (invite and assign groups)
     */
    @Nonnull
    public String getPrincipalBase() {
        return getProperty(PN_PRINCIPAL_BASE, "tenant/" + getId());
    }

    // Object

    public String toString() {
        return new StringBuilder("tenant{id:").append(getId())
                .append(",").append("name:").append(getName())
                .append(",").append("status:").append(getStatus())
                .append("}").toString();
    }
}
