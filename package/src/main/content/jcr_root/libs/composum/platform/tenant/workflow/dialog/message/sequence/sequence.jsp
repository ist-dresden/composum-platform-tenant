<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sling:defineObjects/>
<cpn:component id="task" type="com.composum.platform.workflow.model.WorkflowTaskInstance">
    <cpn:div test="${not empty task.head}" class="conversation">
        <c:forEach items="${task.head}" var="head" varStatus="status">
            <div class="conversation-item">
                <c:if test="${not status.first}">
                    <button type="button"
                            class="conversation-toggle fa fa-level-up btn btn-xs btn-default"></button>
                </c:if>
                <div class="conversation-meta">
                    <cpn:text value="${head.date}"/> |
                    <cpn:text value="${head.data.from}"/>
                </div>
                <cpn:div test="${not empty head.data.subject and task.data.subject ne head.data.subject}"
                         class="composum-platform-workflow_message-subject">
                    <cpn:text value="${head.data.subject}"/>
                </cpn:div>
                <cpn:div test="${not empty task.data.message}" class="composum-platform-workflow_message-body">
                    <cpn:text value="${head.data.message}" type="rich"/>
                </cpn:div>
            </div>
        </c:forEach>
        <div class="conversation-start">
            <button type="button"
                    class="conversation-toggle btn btn-xs btn-default">${cpn:i18n(slingRequest,'Conversation')}</button>
        </div>
    </cpn:div>
</cpn:component>
