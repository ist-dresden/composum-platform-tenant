package com.composum.platform.tenant.workflow.user;

import com.composum.platform.models.simple.MetaData;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.workflow.WorkflowAction;
import com.composum.platform.workflow.model.WorkflowTaskInstance;
import com.composum.platform.workflow.model.WorkflowTaskTemplate;
import com.composum.sling.core.BeanContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * with 'revoke' the roles in the data will be removed from the user
 */
@SuppressWarnings("Duplicates")
@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Revoke Tenant Role Workflow Job",
                JobConsumer.PROPERTY_TOPICS + "=" + RevokeRole.JOB_TOPIC
        }
)
public class RevokeRole implements WorkflowAction {

    public static final String JOB_TOPIC = "composum/platform/tenant/workflow/revoke-role";

    @Reference
    private TenantUserManager userManager;

    @Override
    @Nonnull
    public Result process(@Nonnull final BeanContext context,
                          @Nonnull final WorkflowTaskInstance task,
                          @Nullable final WorkflowTaskTemplate.Option option, @Nullable final String comment,
                          @Nonnull final MetaData metaData) {
        try {
            String tenantId = task.getData().get("tenantId", "");
            String userId = task.getData().get("userId", "");
            String[] role = task.getData().get("role", new String[0]);
            if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(userId)) {
                userManager.revoke(context.getResolver(), tenantId, userId, role);
                return Result.OK;
            } else {
                return new Result(Status.failure, new Message(Level.error, "tenant and user must be specified"));
            }
        } catch (Exception ex) {
            return new Result(Status.failure, new Message(Level.error, ex.toString()));
        }
    }
}
