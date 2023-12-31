package com.composum.platform.tenant.workflow.tenant;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.workflow.WorkflowAction;
import com.composum.platform.workflow.model.WorkflowTaskInstance;
import com.composum.platform.workflow.model.WorkflowTaskTemplate;
import com.composum.sling.core.BeanContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.tenant.Tenant;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * with 'assign' the roles in the data will be assigned by keeping existing assignments
 */
@SuppressWarnings("Duplicates")
@Component(
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Create a new Tenant Workflow Job",
                JobConsumer.PROPERTY_TOPICS + "=" + CreateTenant.JOB_TOPIC
        }
)
public class CreateTenant implements WorkflowAction {

    public static final Logger LOG = LoggerFactory.getLogger(CreateTenant.class);

    public static final String JOB_TOPIC = "composum/platform/tenant/workflow/tenant/create";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private TenantManagerService tenantManager;

    @Reference
    private TenantUserManager userManager;

    @Override
    @Nonnull
    public Result process(@Nonnull final BeanContext context, @Nonnull final WorkflowTaskInstance task,
                          @Nullable final WorkflowTaskTemplate.Option option, @Nonnull final ValueMap data) {
        try {
            String tenantId = data.get("tenantId", "");
            String userId = data.get("userId", "");
            if (LOG.isInfoEnabled()) {
                LOG.info("process ({},{})", tenantId, userId);
            }
            if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(userId)) {
                Map<String, Object> tenantProperties = new HashMap<>();
                String value;
                if (StringUtils.isNotBlank(value = data.get("name", ""))) {
                    tenantProperties.put(Tenant.PROP_NAME, value);
                }
                if (StringUtils.isNotBlank(value = data.get("description", ""))) {
                    tenantProperties.put(Tenant.PROP_DESCRIPTION, value);
                }
                try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(null)) {
                    Tenant tenant = tenantManager.createTenant(resolver, tenantId, tenantProperties);
                    try {
                        userManager.assign(resolver, tenant.getId(), userId,
                                TenantUserManager.Role.manager.name(),
                                TenantUserManager.Role.publisher.name(),
                                TenantUserManager.Role.editor.name());
                    } catch (RepositoryException ex) {
                        LOG.error(ex.getMessage(), ex);
                        return new Result(Status.failure, new Message(Level.error, "can't join user '{}' to '{}' ({})",
                                userId, tenantId, ex.getMessage()));
                    }
                    resolver.commit();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("create tenant successful ({},{})", tenantId, userId);
                    }
                    return Result.OK;
                } catch (PersistenceException ex) {
                    LOG.error(ex.getMessage(), ex);
                    return new Result(Status.failure, new Message(Level.error, "can't create tenant '{}' ({})",
                            tenantId, ex.getMessage()));
                }
            } else {
                LOG.error("process failed, no tenant ({}) or user ({})", tenantId, userId);
                return new Result(Status.failure, new Message(Level.error, "tenant and user must be specified"));
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return new Result(Status.failure, new Message(Level.error, ex.toString()));
        }
    }
}
