<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<div class="form-group ">
    <cpn:text>Select the site and the sites stage ('public' or 'preview') which should be mapped to your host.</cpn:text>
    <cpn:text>Such a mapping must be unique, prevent from mapping of the same site and stage to more than one host.</cpn:text>
</div>
