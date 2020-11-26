package com.composum.platform.tenant.view;

import com.composum.pages.commons.model.Site;
import com.composum.pages.commons.service.SiteManager;
import com.composum.platform.tenant.service.HostManagerService;
import com.composum.platform.tenant.service.HostManagerService.Host;
import com.composum.platform.tenant.service.HostManagerService.HostList;
import com.composum.platform.tenant.service.TenantManagerService;
import com.composum.platform.tenant.service.TenantUserManager;
import com.composum.platform.tenant.service.TenantUserManager.TenantUsers;
import com.composum.platform.tenant.service.impl.PlatformTenant;
import com.composum.platform.workflow.model.WorkflowTaskInstance;
import com.composum.platform.workflow.service.WorkflowService;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.filter.ResourceFilter;
import com.composum.sling.core.util.XSS;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.composum.platform.tenant.servlet.HostManagerServlet.PARAM_HOSTNAME;

public class TenantBean extends AbstractTenantBean {

    private static final Logger LOG = LoggerFactory.getLogger(TenantBean.class);

    public static final String STATUS_INACTIVE = "inactive";

    public static final String ATTR_HOST = "host";
    public static final String ATTR_HOSTNAME = "hostname";

    public static final String FMT_TIME = "yyyy-MM-dd HH:mm";

    private transient TenantUsers users;
    private transient Calendar lastLogin;

    private transient HostList hosts;
    private transient Host host;

    private transient List<TenantSite> tenantSites;
    private transient List<TenantHost> tenantHosts;

    private transient List<SiteOption> siteOptions;

    private transient Integer activeWorkflows;
    private transient Calendar lastWorkflowActivity;

    private transient TenantManagerService manager;
    private transient TenantUserManager userManager;
    private transient HostManagerService hostManager;

    public TenantBean(BeanContext context, Resource resource) {
        super(context, resource);
    }

    public TenantBean(BeanContext context) {
        super(context);
    }

    public TenantBean() {
        super();
    }

    protected TenantManagerService getManager() {
        if (manager == null) {
            manager = context.getService(TenantManagerService.class);
        }
        return manager;
    }

    public String getHintSelector() {
        String[] selectors = request.getRequestPathInfo().getSelectors();
        return selectors.length > 1 ? selectors[1] : "";
    }

    public String getId() {
        return getTenant().getId();
    }

    public String getTitle() {
        return getTenant().getName();
    }

    public String getStatus() {
        String result = "";
        PlatformTenant.Status status = getTenant().getStatus();
        if (status != null) {
            result = status.name();
            if (status == PlatformTenant.Status.active) {
                int siteCount = getTenantSites().size();
                if (siteCount == 0) {
                    result = STATUS_INACTIVE;
                } else {
                    Calendar lastLogin = getLastLogin();
                    Calendar toCompare = Calendar.getInstance();
                    toCompare.add(Calendar.MONTH, -3);
                    if (lastLogin == null || lastLogin.before(toCompare)) {
                        result = STATUS_INACTIVE;
                    }
                }
            }
        }
        return result;
    }

    // users

    public int getCountUsers() {
        return getUsers().size();
    }

    public TenantUsers getUsers() {
        if (users == null) {
            try {
                users = getUserManager().getTenantUsers(context.getResolver(), getTenant().getId());
            } catch (RepositoryException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return users;
    }

    public String getLastLoginString() {
        Calendar lastLogin = getLastLogin();
        return lastLogin != null ? new SimpleDateFormat(FMT_TIME).format(lastLogin.getTime()) : "--";
    }

    public Calendar getLastLogin() {
        if (lastLogin == null) {
            for (TenantUserManager.TenantUser user : getUsers()) {
                Calendar ll = user.getLastLogin();
                if (ll != null && (lastLogin == null || lastLogin.before(ll))) {
                    lastLogin = ll;
                }
            }
        }
        return lastLogin;
    }

    protected TenantUserManager getUserManager() {
        if (userManager == null) {
            userManager = context.getService(TenantUserManager.class);
        }
        return userManager;
    }

    // hosts

    public String getPublicHostname() {
        return getHostManager().getPublicHostname();
    }

    public int getCountHosts() {
        return getHosts().size();
    }

    public Host getHost() {
        if (host == null) {
            SlingHttpServletRequest request = getRequest();
            host = (Host) request.getAttribute(ATTR_HOST);
            if (host == null) {
                String hostname = (String) request.getAttribute(ATTR_HOSTNAME);
                if (StringUtils.isBlank(hostname)) {
                    hostname = XSS.filter(request.getParameter(PARAM_HOSTNAME));
                }
                if (StringUtils.isNotBlank(hostname)) {
                    host = getHosts().get(hostname);
                }
            }
        }
        return host;
    }

    public HostList getHosts() {
        if (hosts == null) {
            try {
                Tenant tenant = getTenant();
                hosts = getHostManager().hostList(getResolver(), tenant != null ? tenant.getId() : null);
            } catch (HostManagerService.ProcessException ex) {
                LOG.info(ex.toString());
                hosts = new HostList();
            }
        }
        return hosts;
    }

    protected HostManagerService getHostManager() {
        if (hostManager == null) {
            hostManager = context.getService(HostManagerService.class);
        }
        return hostManager;
    }

    // Sites

    public class SiteOption {

        private final Site site;

        public SiteOption(Site site) {
            this.site = site;
        }

        public Site getSite() {
            return site;
        }

        public String getPath() {
            return site.getPath();
        }

        public String getTitle() {
            return site.getTitle();
        }

        public String getLabel() {
            return site.getTitle() + " (" + site.getPath() + ")";
        }

        public String getReplicationConfig() {
            return "/conf" + getPath() + "/replication";
        }

        public boolean isSelected() {
            Host host = getHost();
            if (host != null) {
                return site.getPath().equals(host.getSiteRef());
            }
            return false;
        }
    }

    public Collection<SiteOption> getSiteOptions() {
        if (siteOptions == null) {
            ResourceResolver resolver = context.getResolver();
            Resource sitesRoot = resolver.getResource(getTenant().getContentRoot());
            siteOptions = new ArrayList<>();
            for (Site site : context.getService(SiteManager.class).getSites(context, sitesRoot, ResourceFilter.ALL)) {
                siteOptions.add(new SiteOption(site));
            }
            siteOptions.sort(Comparator.comparing(SiteOption::getLabel));
        }
        return siteOptions;
    }

    public class TenantHost {

        @Nullable
        protected final String stage; // 'null' if host is not joined to a site

        @Nonnull
        protected final Host host;

        public TenantHost(@Nullable final String stage,
                          @Nonnull final Host host) {
            this.stage = stage;
            this.host = host;
        }

        @Nonnull
        public String getHostname() {
            return host.getHostname();
        }
    }

    public class TenantSite {

        @Nonnull
        protected final Site site;

        @Nullable
        protected final TenantHost publicHost;
        @Nullable
        protected final TenantHost previewHost;

        public TenantSite(@Nonnull final Site site,
                          @Nullable final TenantHost publicHost,
                          @Nullable final TenantHost previewHost) {
            this.site = site;
            this.publicHost = publicHost;
            this.previewHost = previewHost;
        }

        @Nonnull
        public Site getSite() {
            return site;
        }

        @Nonnull
        public String getPath() {
            return site.getPath();
        }

        @Nonnull
        public String getTitle() {
            return site.getTitle();
        }

        @Nullable
        public String getDescription() {
            return site.getDescription();
        }

        @Nullable
        public TenantHost getPublicHost() {
            return publicHost;
        }

        @Nullable
        public TenantHost getPreviewHost() {
            return previewHost;
        }
    }

    public int getCountSites() {
        return getTenantSites().size();
    }

    public Collection<TenantSite> getTenantSites() {
        if (tenantSites == null) {
            ResourceResolver resolver = context.getResolver();
            tenantSites = new ArrayList<>();
            tenantHosts = new ArrayList<>();
            HostList unassignedHosts = new HostList();
            unassignedHosts.addAll(getHosts());
            Resource sitesRoot = resolver.getResource(getTenant().getContentRoot());
            for (Site site : context.getService(SiteManager.class).getSites(context, sitesRoot, ResourceFilter.ALL)) {
                tenantSites.add(new TenantSite(site,
                        getJoinedHost(unassignedHosts, site, "public"),
                        getJoinedHost(unassignedHosts, site, "preview")));
            }
            tenantSites.sort(Comparator.comparing(TenantSite::getTitle));
            for (Host host : unassignedHosts) {
                tenantHosts.add(new TenantHost(null, host));
            }
        }
        return tenantSites;
    }

    public Collection<TenantHost> getTenantHosts() {
        if (tenantHosts == null) {
            getTenantSites();
        }
        return tenantHosts;
    }

    protected TenantHost getJoinedHost(HostList unassignedHosts, Site site, String stage) {
        for (Host host : getHosts()) {
            if (site.getPath().equals(host.getSiteRef())
                    && stage.equals(host.getSiteStage())) {
                unassignedHosts.remove(host);
                return new TenantHost(stage, host);
            }
        }
        return null;
    }

    // Workflow

    public int getActiveWorkflows() {
        if (activeWorkflows == null) {
            activeWorkflows = 0;
            lastWorkflowActivity = null;
            WorkflowService workflowService = context.getService(WorkflowService.class);
            if (workflowService != null) {
                Iterator<WorkflowTaskInstance> tasks = workflowService.findTasks(context, getId(), WorkflowTaskInstance.State.pending);
                while (tasks.hasNext()) {
                    activeWorkflows++;
                    WorkflowTaskInstance task = tasks.next();
                    Calendar created = task.getCreated();
                    if (lastWorkflowActivity == null || lastWorkflowActivity.before(created)) {
                        lastWorkflowActivity = created;
                    }
                }
            }
        }
        return activeWorkflows;
    }

    public String getLastWorkflowActivityString() {
        Calendar lastActivity = getLastWorkflowActivity();
        return lastActivity != null ? new SimpleDateFormat(FMT_TIME).format(lastActivity.getTime()) : "--";
    }

    public Calendar getLastWorkflowActivity() {
        if (lastWorkflowActivity == null) {
            getActiveWorkflows();
        }
        return lastWorkflowActivity;
    }
}
