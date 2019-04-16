package com.composum.platform.tenant.widget.select;

import com.composum.pages.commons.widget.ComboBox;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.workflow.model.WorkflowTaskInstance;
import com.composum.platform.workflow.service.WorkflowService;
import com.composum.sling.cpnl.CpnlElFunctions;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the Widget model for the assignee combobox widget (derived from the pages ComboBox widget model)
 */
public class Assignee extends ComboBox {

    protected static final Pattern TENANT_ROLE_GROUP = Pattern.compile("tenant-([^-]+)-(.+)s$");

    /**
     * @return the value mapped to the role if the value is a tenant group id
     */
    @Override
    public String getValue() {
        String value = super.getValue();
        if (StringUtils.isNotBlank(value)) {
            Matcher matcher = TENANT_ROLE_GROUP.matcher(value);
            if (matcher.matches()) {
                value = matcher.group(2);
            }
        }
        return StringUtils.isNotBlank(value) ? value : getDefaultValue();
    }

    /**
     * @return the context resources task assignee as the default value
     * (the resource itself or the resource referenced by the requests suffix)
     */
    @Override
    public String getDefaultValue() {
        String value = "";
        WorkflowService workflowService = context.getService(WorkflowService.class);
        WorkflowTaskInstance task = null;
        Resource resource = getResource();
        if (resource != null) {
            task = workflowService.getInstance(context, resource.getPath());
        }
        if (task == null) {
            SlingHttpServletRequest request = context.getRequest();
            if (request != null) {
                RequestPathInfo pathInfo = request.getRequestPathInfo();
                String suffix = pathInfo.getSuffix();
                if (StringUtils.isNotBlank(suffix)) {
                    resource = context.getResolver().getResource(suffix);
                    if (resource != null) {
                        task = workflowService.getInstance(context, resource.getPath());
                    }
                }
            }
        }
        if (task != null) {
            value = task.getAssignee();
        }
        return value;
    }

    /**
     * @return the list of roles available for selection
     */
    @Nonnull
    protected List<Option> retrieveOptions() {
        SlingHttpServletRequest request = context.getRequest();
        List<Option> options = new ArrayList<>();
        for (TenantUserManager.Role role : TenantUserManager.Role.values()) {
            if (role != TenantUserManager.Role.member) {
                options.add(newOption(request != null
                                ? CpnlElFunctions.i18n(request, StringUtils.capitalize(role.name())) : role.name(),
                        role.name(), null));
            }
        }
        return options;
    }
}
