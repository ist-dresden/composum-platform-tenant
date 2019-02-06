package com.composum.platform.tenant.setup;

import com.composum.sling.core.service.RepositorySetupService;
import com.composum.sling.core.setup.util.SetupUtil;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

@SuppressWarnings("Duplicates")
public class SetupHook implements InstallHook {

    private static final Logger LOG = LoggerFactory.getLogger(SetupHook.class);

    private static final String SETUP_ACLS = "/conf/composum/platform/tenant/acl/setup.json";

    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public void execute(InstallContext ctx) {
        switch (ctx.getPhase()) {
            case INSTALLED:
                LOG.info("installed: execute...");
                registerHome(ctx);
                setupAcls(ctx);
                LOG.info("installed: execute ends.");
                break;
        }
    }

    protected void registerHome(InstallContext ctx) {
        try {
            Session session = ctx.getSession();
            Node root = session.getNode("/");
            root.setProperty("sling:target", "/libs/composum/platform/home.html");
            session.save();
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    protected void setupAcls(InstallContext ctx) {
        RepositorySetupService setupService = SetupUtil.getService(RepositorySetupService.class);
        try {
            Session session = ctx.getSession();
            setupService.addJsonAcl(session, SETUP_ACLS, null);
            session.save();
        } catch (RepositoryException | IOException rex) {
            LOG.error(rex.getMessage(), rex);
        }
    }
}
