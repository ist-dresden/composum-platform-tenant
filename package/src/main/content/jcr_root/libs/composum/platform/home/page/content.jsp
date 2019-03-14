<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="home" type="com.composum.platform.tenant.view.HomePageBean">
    <div class="composum-platform-public_content">
        <sling:include path="/libs/composum/platform/public/page" replaceSelectors="header"/>
        <div class="composum-platform-public_panel panel panel-default">
            <div class="panel-body">
                <c:choose>
                    <c:when test="${home.tenantsAvailable}">
                        <cpn:text tagName="div" class="alert alert-danger" value="${home.properties.noSites}"
                                  type="rich"/>
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${false and home.openWorkflows}">
                                <cpn:text tagName="div" class="alert alert-warning" value="${home.properties.openTasks}"
                                          type="rich"/>
                            </c:when>
                            <c:otherwise>
                                <cpn:text tagName="div" class="alert alert-warning" value="${home.properties.noTenant}"
                                          type="rich"/>
                                <cpn:text tagName="div" class="alert alert-success" value="${home.properties.invitation}"
                                          type="rich"/>
                                <sling:call script="invitation.jsp"/>
                                <hr/>
                                <cpn:text tagName="div" class="alert alert-success" value="${home.properties.creation}"
                                          type="rich"/>
                                <sling:call script="creation.jsp"/>
                            </c:otherwise>
                        </c:choose>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <sling:include path="/libs/composum/platform/public/page" replaceSelectors="footer"/>
    </div>
</cpn:component>
