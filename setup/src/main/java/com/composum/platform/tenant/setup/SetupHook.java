package com.composum.platform.tenant.setup;

import com.composum.sling.core.service.RepositorySetupService;
import com.composum.sling.core.setup.util.SetupUtil;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallHook;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

@SuppressWarnings("Duplicates")
public class SetupHook implements InstallHook {

    private static final Logger LOG = LoggerFactory.getLogger(SetupHook.class);

    private static final String SETUP_ACLS = "/conf/composum/platform/tenant/acl/setup.json";

    private static final String[] EVERYONE_ACLS = {
            "/conf/composum/platform/security/acl/everyone.json",
            "/conf/composum/platform/tenant/acl/everyone.json",
            "/conf/composum/platform/tenant/acl/setup.json"
    };

    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public void execute(InstallContext ctx) throws PackageException {
        switch (ctx.getPhase()) {
            case INSTALLED:
                LOG.info("installed: execute...");
                registerHome(ctx);
                setupUsers(ctx);
                setupAcls(ctx);
                LOG.info("installed: execute ends.");
                break;
        }
    }

    protected void registerHome(InstallContext ctx) {
        try {
            Session session = ctx.getSession();
            session.refresh(false);
            Node root = session.getNode("/");
            root.setProperty("sling:target", "/cpm/home.html");
            session.save();
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    protected void setupUsers(InstallContext ctx) throws PackageException {
        try {
            Session session = ctx.getSession();
            session.refresh(false);
            SetupUtil.setupGroupsAndUsers(ctx,
                    new HashMap<String, List<String>>() {{
                        put("system/composum/platform/composum-platform-services", emptyList());
                        put("composum/platform/composum-platform-tenant-members", emptyList());
                        put("composum/platform/composum-platform-tenant-visitors", emptyList());
                        put("composum/platform/composum-platform-tenant-publishers", emptyList());
                        put("composum/platform/composum-platform-tenant-editors", emptyList());
                        put("composum/platform/composum-platform-tenant-developers", emptyList());
                        put("composum/platform/composum-platform-tenant-managers", emptyList());
                        put("composum/platform/composum-platform-tenant-assistants", emptyList());
                    }},
                    singletonMap("system/composum/platform/composum-platform-tenant-service",
                            new ArrayList<String>() {{
                                add("composum-platform-services");
                            }}),
                    null);
            session.save();
        } catch (RuntimeException | RepositoryException e) {
            LOG.error("" + e, e);
            throw new PackageException(e);
        }
    }

    /**
     * Insert all ACLs.
     * Due to simultaneous package imports there have been conflicts when modifying /conf/content, so we retry if it fails.
     */
    protected void setupAcls(InstallContext ctx) throws PackageException {
        RepositorySetupService setupService = SetupUtil.getService(RepositorySetupService.class);
        try {
            trySetupAcls(ctx, setupService);
        } catch (Exception rex) {
            LOG.error("Setting up ACL failed, retrying: {}", rex.toString());
            try {
                Thread.sleep(1000);
                trySetupAcls(ctx, setupService);
            } catch (Exception rex2) {
                LOG.error("Setting up ACL failed, retrying (2): {}", rex2.toString());
                try {
                    Thread.sleep(5000);
                    trySetupAcls(ctx, setupService);
                } catch (Exception rex3) {
                    LOG.error("Setting up ACL failed, giving up.", rex3);
                    throw new PackageException(rex);
                }
            }
        }
    }

    private void trySetupAcls(InstallContext ctx, RepositorySetupService setupService) throws RepositoryException, IOException {
        Session session = ctx.getSession();
        session.refresh(false);
        setupService.addJsonAcl(session, SETUP_ACLS, null);
        for (String script : EVERYONE_ACLS) {
            setupService.addJsonAcl(session, script, null);
        }
        session.refresh(true); // try to avoid conflicts with parallel changes by other package imports
        session.save();
    }
}
