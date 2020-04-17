<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>



<%@ attribute name="id" required="true"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="labelKeyForExpired"%>
<%@ attribute name="disabled"%>
<%@ attribute name="configurationUrl"%>
<%@ attribute name="configurationJson"%>
<%@ attribute name="makeStickyOnScroll"%>
<%@ attribute name="taskDueDateDisplayDivId"%>
<%@ attribute name="cssClass"%>

<c:set var="colSpanToUse" value="12" scope="page" />
<c:if test="${not empty colSpan}">
	<c:set var="colSpanToUse" value="${colSpan}" scope="page" />
</c:if>
<spring:message code="${labelKey}" var="displayLabel"
	text="Time Remaining" />
<spring:message code="${labelKeyForExpired}" var="displayLabelExpired"
	text="Time Exceeded" />
<spring:message code="label.tatTimerTag.dueDate" var="dueDateLabel"
	text="Due Date" />

<div id="timer_container<c:out value='${id}'/>" class="tatTimer_container span<c:out value='${colSpanToUse}' /> <c:out value='${cssClass}'/>"
	data-timer-disabled="<c:out value='${disabled}'/>" data-config-url="<c:out value='${configurationUrl}'/>"
	data-config-json="<c:out value='${configurationJson}'/>"
	data-label-message="${displayLabel}"
	data-label-message-expired="${displayLabelExpired}"
	data-make-sticky="<c:out value='${makeStickyOnScroll}'/>"
	data-date-divid="<c:out value='${taskDueDateDisplayDivId}'/>"
	data-date-label="${dueDateLabel}"   data-context-key="<c:out value='${id}'/>">
	<div id="handler<c:out value='${id}'/>" class="hide tatTimer_handlerTimer" data-context-key="<c:out value='${id}'/>"></div>
	<div id="<c:out value='${id}'/>" class="tat_display tatTimer_mainTimer" data-context-key="<c:out value='${id}'/>"></div>
</div>