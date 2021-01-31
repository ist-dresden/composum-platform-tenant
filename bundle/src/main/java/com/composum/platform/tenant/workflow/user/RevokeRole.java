package com.composum.platform.tenant.workflow.user;

import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.workflow.WorkflowAction;
import com.composum.platform.workflow.model.WorkflowTaskInstance;
import com.composum.platform.workflow.model.WorkflowTaskTemplate;
import com.composum.sling.core.BeanContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * with 'revoke' the roles in the data will be removed from the user
 */
@SuppressWarnings("Duplicates")
@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Revoke Tenant Role Workflow Job",
                JobConsumer.PROPERTY_TOPICS + "=" + RevokeRole.JOB_TOPIC
        }
)
public class RevokeRole implements WorkflowAction {

    public static final Logger LOG = LoggerFactory.getLogger(RevokeRole.class);

    public static final String JOB_TOPIC = "composum/platform/tenant/workflow/revoke-role";

    @Reference
    private TenantUserManager userManager;

    @Override
    @Nonnull
    public Result process(@Nonnull final BeanContext context, @Nonnull final WorkflowTaskInstance task,
                          @Nullable final WorkflowTaskTemplate.Option option, @Nonnull final ValueMap data) {
        try {
            String tenantId = data.get("tenantId", "");
            String userId = data.get("userId", "");
            String[] role = data.get("role", new String[0]);
            if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(userId)) {
                userManager.revoke(context.getResolver(), tenantId, userId, role);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("revoke successful ({},{}) - [{}]", tenantId, userId, StringUtils.join(role, ", "));
                }
                return Result.OK;
            } else {
                LOG.error("revoke failed, no tenant ({}) or user ({})", tenantId, userId);
                return new Result(Status.failure, new Message(Level.error, "tenant and user must be specified"));
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return new Result(Status.failure, new Message(Level.error, ex.toString()));
        }
    }
}
