<%@page session="false" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<sling:defineObjects/>
<div class="form-group">
    <label class="control-label">User ID</label>
    <input name="user.id" class="widget text-field-widget form-control" type="text"
           data-rules="mandatory"/>
</div>
