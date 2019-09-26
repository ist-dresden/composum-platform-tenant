package com.composum.platform.tenant.widget.select;

import com.composum.pages.commons.widget.Select;
import com.composum.platform.tenant.service.TenantManagerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.tenant.Tenant;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * the Widget model for the tenant select widget (derived from the pages Select widget model)
 */
public class TenantSelect extends Select {

    /**
     * @return the context resources tenant as the default value
     * (the resource itself or the resource referenced by the requests suffix)
     */
    @Override
    public String getDefaultValue() {
        String value = "";
        Tenant tenant = null;
        Resource resource = getResource();
        if (resource != null) {
            tenant = resource.adaptTo(Tenant.class);
        }
        if (tenant == null) {
            SlingHttpServletRequest request = context.getRequest();
            if (request != null) {
                RequestPathInfo pathInfo = request.getRequestPathInfo();
                String suffix = pathInfo.getSuffix();
                if (StringUtils.isNotBlank(suffix)) {
                    resource = context.getResolver().getResource(suffix);
                    if (resource != null) {
                        tenant = resource.adaptTo(Tenant.class);
                    }
                }
            }
        }
        if (tenant != null) {
            value = tenant.getId();
        }
        return value;
    }

    /**
     * @return the list of tenants available for selection
     */
    @Nonnull
    protected List<Option> retrieveOptions() {
        List<Option> options = new ArrayList<>();
        TenantManagerService managerService = context.getService(TenantManagerService.class);
        if (managerService != null) {
            Iterator<Tenant> tenants = managerService.getTenants(context.getResolver(), null);
            while (tenants.hasNext()) {
                Tenant tenant = tenants.next();
                StringBuilder label = new StringBuilder();
                String name = tenant.getName();
                if (StringUtils.isNotBlank(name)) {
                    label.append(name).append(" ");
                }
                label.append("(").append(tenant.getId()).append(")");
                options.add(newOption(label.toString(), tenant.getId(), null));
            }
            options.sort(Comparator.comparing(Option::getLabel));
        }
        return options;
    }
}
