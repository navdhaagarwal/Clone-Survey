<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ attribute name="action"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="modelAttribute" required="true"%>
<%@ attribute name="validatorEnabled"%>
<%@ attribute name="method"%>
<%@ attribute name="autoComplete"%>


<c:set var="validatorClass" value="form" scope="page" />

<c:set var="validatorEnabledFlag" value="true" scope="page" />
<c:if test="${not empty validatorEnabled}">
	<c:if test="${validatorEnabled eq false}">
		<c:set var="validatorClass" value="" scope="page" />
	</c:if>
</c:if>

<c:if test="${not empty action}">
	<c:set var="actionClass" value="${action}" scope="page" />
</c:if>

<c:if test="${not empty validators}">
	<c:set var="validatorClass" value="${validators}" scope="page" />
</c:if>

<c:if test="${empty method}">
	<c:set var="method" value="GET" scope="page" />
</c:if>

<c:if test="${not empty method}">
	<c:set var="method" value="${method}" scope="page" />
</c:if>

<c:if test="${empty autoComplete}">
	<c:set var="autoComplete" value="on" scope="page" />
</c:if>

<form:form id="${id}" method="${method}" modelAttribute="${modelAttribute}"
	action="${actionClass}" autocomplete="${autoComplete}" cssClass="form">
	<jsp:doBody></jsp:doBody>
</form:form>
