package com.composum.platform.tenant.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.tenant.Tenant;

import java.util.Comparator;

public class TenantComparator implements Comparator<Tenant> {

    public static final TenantComparator INSTANCE = new TenantComparator();

    @Override
    public int compare(Tenant o1, Tenant o2) {
        return getKey(o1).compareTo(getKey(o2));
    }

    public static String getKey(Tenant tenant) {
        String key = tenant.getName();
        if (StringUtils.isBlank(key)) {
            key = tenant.getId();
        }
        return key.toLowerCase();
    }
}
