<%@page session="false" pageEncoding="utf-8"%>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2"%>
<sling:defineObjects />
<sling:include resourceType="composum/platform/tenant/manager/dialogs/create"/>
<sling:include resourceType="composum/platform/tenant/manager/dialogs/change"/>
<sling:include resourceType="composum/platform/tenant/manager/dialogs/delete"/>
<sling:include resourceType="composum/platform/tenant/manager/dialogs/activate"/>
<sling:call script="/libs/composum/nodes/console/page/dialogs.jsp"/>
