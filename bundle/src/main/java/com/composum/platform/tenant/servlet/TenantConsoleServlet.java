package com.composum.platform.tenant.servlet;

import com.composum.sling.core.BeanContext;
import com.composum.sling.core.servlet.AbstractConsoleServlet;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.util.regex.Pattern;

@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Composum Platform Tenant Console Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=" + TenantConsoleServlet.SERVLET_PATH,
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET
        })
public class TenantConsoleServlet extends AbstractConsoleServlet {

    public static final String SERVLET_PATH = "/bin/platform/tenants";

    public static final String RESOURCE_TYPE = "composum/platform/tenant/manager";

    public static final String CONSOLE_PATH = "/libs/composum/platform/tenant/manager";

    public static final Pattern PATH_PATTERN = Pattern.compile("^(" + SERVLET_PATH + "(\\.[^/]+)?\\.html)(/.*)?$");

    @Override
    protected String getServletPath(BeanContext context) {
        return SERVLET_PATH;
    }

    @Override
    protected Pattern getPathPattern(BeanContext context) {
        return PATH_PATTERN;
    }

    @Override
    protected String getResourceType(BeanContext context) {
        return RESOURCE_TYPE;
    }

    @Override
    protected String getConsolePath(BeanContext context) {
        return CONSOLE_PATH;
    }
}
