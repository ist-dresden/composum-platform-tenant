package com.composum.platform.tenant.workflow.user;

import com.composum.platform.models.simple.MetaData;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.workflow.WorkflowAction;
import com.composum.platform.workflow.model.WorkflowTaskInstance;
import com.composum.platform.workflow.model.WorkflowTaskTemplate;
import com.composum.platform.workflow.service.WorkflowService;
import com.composum.sling.core.BeanContext;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Assign Tenant Role Workflow Job",
                JobConsumer.PROPERTY_TOPICS + "=" + AssignRole.JOB_TOPIC
        }
)
public class AssignRole implements WorkflowAction {

    public static final String JOB_TOPIC = "composum/platform/tenant/workflow/assign-role";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private WorkflowService workflowService;

    @Reference
    private TenantManagerService tenantManager;

    @Override
    @Nonnull
    public Result process(@Nonnull final BeanContext context,
                          @Nonnull final WorkflowTaskInstance task,
                          @Nullable final WorkflowTaskTemplate.Option option, @Nullable final String comment,
                          @Nonnull final MetaData metaData) {
        try (ResourceResolver jobResolver = resolverFactory.getServiceResourceResolver(null)) {

            return Result.OK;
        } catch (Exception ex) {
            return new Result(Status.failure, new Message(Level.error, ex.toString()));
        }
    }
}
