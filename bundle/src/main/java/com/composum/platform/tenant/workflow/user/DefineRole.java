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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * with 'define' the roles in the data will be assigned all other available roles will be revoked
 */
@SuppressWarnings("Duplicates")
@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Define Tenant Role Workflow Job",
                JobConsumer.PROPERTY_TOPICS + "=" + DefineRole.JOB_TOPIC
        }
)
public class DefineRole implements WorkflowAction {

    public static final Logger LOG = LoggerFactory.getLogger(DefineRole.class);

    public static final String JOB_TOPIC = "composum/platform/tenant/workflow/define-role";

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
                userManager.define(context.getResolver(), tenantId, userId, role);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("define successful ({},{}) - [{}]", tenantId, userId, StringUtils.join(role, ", "));
                }
                return Result.OK;
            } else {
                LOG.error("define failed, no tenant ({}) or user ({})", tenantId, userId);
                return new Result(Status.failure, new Message(Level.error, "tenant and user must be specified"));
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return new Result(Status.failure, new Message(Level.error, ex.toString()));
        }
    }
}
