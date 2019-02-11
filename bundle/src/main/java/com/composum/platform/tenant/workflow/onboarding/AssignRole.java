package com.composum.platform.tenant.workflow.onboarding;

import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.workflow.service.WorkflowService;
import com.composum.platform.workflow.model.WorkflowTask;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        property = {
                JobConsumer.PROPERTY_TOPICS + "=" + AssignRole.JOB_TOPIC
        }
)
public class AssignRole implements JobConsumer {

    public static final String JOB_TOPIC = "platform.tenant.workflow.assign-role";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private WorkflowService workflowService;

    @Reference
    private TenantManagerService tenantManager;

    @Override
    public JobResult process(Job job) {
        try (ResourceResolver jobResolver = resolverFactory.getServiceResourceResolver(null)) {
            WorkflowTask task = workflowService.getInstance(jobResolver, job);

            return JobResult.OK;
        } catch (Exception ex) {
            return JobResult.FAILED;
        }
    }
}
