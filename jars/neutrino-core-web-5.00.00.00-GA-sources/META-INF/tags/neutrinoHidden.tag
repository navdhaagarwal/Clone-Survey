<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>

<%@ attribute name="id"%>
<%@ attribute name="name"%>
<%@ attribute name="value"%>
<%@ attribute name="path"%>
<%@ attribute name="modificationAllowed"%>



<c:if test="${not empty name}">
	<input type="hidden" name="<c:out value='${name}'/>"
								 id="<c:out value='${id}'/>"
								value="<c:out value='${value}'/>"/>
</c:if>
<c:if test="${not empty path}">
	<spring:bind path="${path}">
		<c:set var="preEvalValue" value="${status.value}"></c:set>
	</spring:bind>
	<form:hidden path="${path}" id="${id}"></form:hidden>
</c:if>
<%
	String val = null;
	String fieldName = null;
	if (name == null) {
		val = (String) jspContext.getAttribute("preEvalValue");
		fieldName = path;
	} else {
		val = (String) jspContext.getAttribute("value");
		fieldName = name;
	}

	try {
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in hidden tag :" + e.getMessage());
	}
%>