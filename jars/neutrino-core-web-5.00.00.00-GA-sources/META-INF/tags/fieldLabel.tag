<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ attribute name="fieldPath"%>
<%@ attribute name="fieldLabelKey" required="true"%>
<%@ attribute name="colSpan" required="true"%>
<%@ attribute name="labelFormatClass" %>


<c:set var="colSpanClass" value="" scope="page" />
<c:if test="${not empty colSpan}">
	<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
</c:if>

<c:if test="${not empty labelFormatClass}">
	<c:set var="formatClass" value="${labelFormatClass}" scope="page" />
</c:if>

<c:if test="${not empty fieldPath}">
	<div class="<c:out value='${colSpanClass}' />">
		<div class="<c:out value='${formatClass}' />">
			<label><b><spring:message code="${fieldLabelKey}" /></b></label>
		</div>
		<label><spring:eval expression="${fieldPath}" /></label>
	</div>
</c:if>

<c:if test="${empty fieldPath}">
<div class="<c:out value='${colSpanClass}' />">
	<div class="<c:out value='${formatClass}' />">
	<label><b><spring:message code="${fieldLabelKey}" /></b></label>
	</div>
</div>
</c:if>