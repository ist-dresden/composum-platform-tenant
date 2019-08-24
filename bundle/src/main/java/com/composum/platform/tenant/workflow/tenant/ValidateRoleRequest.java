package com.composum.platform.tenant.workflow.tenant;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.workflow.WorkflowValidator;
import com.composum.platform.workflow.model.WorkflowTaskTemplate;
import com.composum.sling.core.BeanContext;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

public class ValidateRoleRequest implements WorkflowValidator {

    public static final Logger LOG = LoggerFactory.getLogger(ValidateRoleRequest.class);

    @Nonnull
    @Override
    public Result validate(@Nonnull final BeanContext context, @Nonnull final WorkflowTaskTemplate template,
                           @Nonnull final List<String> target, @Nonnull final ValueMap taskData) {
        Result result = new Result();
        ResourceResolverFactory resolverFactory = context.getService(ResourceResolverFactory.class);
        try (ResourceResolver serviceResolver = resolverFactory.getServiceResourceResolver(null)) {
            TenantManagerService tenantService = context.getService(TenantManagerService.class);
            if (tenantService.getTenant(serviceResolver, taskData.get("tenant.id", "")) == null) {
                result = new Result(Status.failure, new Message(Level.error, "tenant id is not known"));
            }
        } catch (LoginException ex) {
            LOG.error(ex.getMessage());
            result = new Result(Status.failure);
        }
        return result;
    }
}
