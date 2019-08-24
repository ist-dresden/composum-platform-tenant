package com.composum.platform.tenant.workflow.tenant;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.workflow.WorkflowValidator;
import com.composum.platform.workflow.model.WorkflowTaskTemplate;
import com.composum.sling.core.BeanContext;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

public class ValidateTenantRequest implements WorkflowValidator {

    public static final Logger LOG = LoggerFactory.getLogger(ValidateTenantRequest.class);

    @Nonnull
    @Override
    public Result validate(@Nonnull final BeanContext context, @Nonnull final WorkflowTaskTemplate template,
                           @Nonnull final List<String> target, @Nonnull final ValueMap taskData) {
        Result result = new Result();
        TenantManagerService tenantService = context.getService(TenantManagerService.class);
        PersistenceException ex = tenantService.isTenantAllowed(context.getResolver(),
                taskData.get("tenantId", ""), taskData);
        if (ex != null) {
            result = new Result(Status.failure, new Message(Level.error, ex.getMessage()));
        }
        return result;
    }
}
